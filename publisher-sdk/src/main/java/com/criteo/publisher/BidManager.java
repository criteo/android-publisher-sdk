/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher;

import static android.content.ContentValues.TAG;

import android.util.Log;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.csm.MetricSendingQueueConsumer;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.network.BidRequestSender;
import com.criteo.publisher.network.LiveBidRequestSender;
import com.criteo.publisher.util.ApplicationStoppedListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BidManager implements ApplicationStoppedListener {

  /**
   * Default TTL (15 minutes in seconds) overridden on immediate bids (CPM > 0, TTL = 0).
   */
  private static final int DEFAULT_TTL_IN_SECONDS = 15 * 60;

  @NonNull
  @GuardedBy("cacheLock")
  private final SdkCache cache;
  private final Object cacheLock = new Object();

  private final AtomicLong cdbTimeToNextCall = new AtomicLong(0);

  @NonNull
  private final Config config;

  @NonNull
  private final Clock clock;

  @NonNull
  private final AdUnitMapper adUnitMapper;

  @NonNull
  private final BidRequestSender bidRequestSender;

  @NonNull
  private final LiveBidRequestSender liveBidRequestSender;

  @NonNull
  private final BidLifecycleListener bidLifecycleListener;

  @NonNull
  private final MetricSendingQueueConsumer metricSendingQueueConsumer;

  BidManager(
      @NonNull SdkCache sdkCache,
      @NonNull Config config,
      @NonNull Clock clock,
      @NonNull AdUnitMapper adUnitMapper,
      @NonNull BidRequestSender bidRequestSender,
      @NonNull LiveBidRequestSender liveBidRequestSender,
      @NonNull BidLifecycleListener bidLifecycleListener,
      @NonNull MetricSendingQueueConsumer metricSendingQueueConsumer
  ) {
    this.cache = sdkCache;
    this.config = config;
    this.clock = clock;
    this.adUnitMapper = adUnitMapper;
    this.bidRequestSender = bidRequestSender;
    this.liveBidRequestSender = liveBidRequestSender;
    this.bidLifecycleListener = bidLifecycleListener;
    this.metricSendingQueueConsumer = metricSendingQueueConsumer;
  }

  /**
   * Notify the given listener for bid or no bid for the given ad unit.
   * <p>
   * {@link BidListener#onBidResponse(CdbResponseSlot)} is invoked only if a bid is available and valide.
   *
   * @param adUnit ad unit to get a bid from (nullable only to accommodate callers)
   * @param bidListener listener to notify
   */
  public void getBidForAdUnit(@Nullable AdUnit adUnit, @NonNull BidListener bidListener) {
    if (adUnit == null) {
      bidListener.onNoBid();
      return;
    }

    if (config.isLiveBiddingEnabled()) {
      getLiveBidForAdUnit(adUnit, bidListener);
    } else {
      CdbResponseSlot cdbResponseSlot = getBidForAdUnitAndPrefetch(adUnit);
      if (cdbResponseSlot != null) {
        bidListener.onBidResponse(cdbResponseSlot);
      } else {
        bidListener.onNoBid();
      }
    }
  }

  /**
   * Returns the last fetched bid a fetch a new one for the next invocation.
   * <p>
   * A <code>null</code> value could be returned. This means that there is no valid bid for the
   * given {@link AdUnit}. And caller should not try to display anything.
   * <code>null</code> may be returned in case of
   * <ul>
   *   <li>The kill switch is engaged. See {@link Config#isKillSwitchEnabled()}</li>
   *   <li>The given {@link AdUnit} is not valid. See {@link AdUnitMapper} for validity definition</li>
   *   <li>There is no last fetch bid or last is consumed</li>
   *   <li>Last fetch bid correspond to a no-bid (CPM = 0 and TTL = 0)</li>
   *   <li>Last fetch bid is a not-expired silence (CPM = 0 and TTL > 0)</li>
   *   <li>Last fetch bid is expired</li>
   * </ul>
   * <p>
   * Asynchronously, a new bid is fetch to CDB to get a new proposition. Hence if this method
   * returns a bid, it is consumed, and you have to wait for the new proposition to get a result
   * again. Meanwhile, you'll only get a <code>null</code> value.
   * There may be some case when a new bid is not fetch:
   * <ul>
   *   <li>The kill switch is engaged</li>
   *   <li>The given {@link AdUnit} is not valid</li>
   *   <li>Last fetch bid is a not-expired silence</li>
   *   <li>There is already an async call to CDB for the given {@link AdUnit}</li>
   * </ul>
   *
   * @param adUnit Declaration of ad unit to get a bid from
   * @return a valid bid that may be displayed or <code>null</code> that should be ignored
   */
  @Nullable
  @VisibleForTesting
  CdbResponseSlot getBidForAdUnitAndPrefetch(@Nullable AdUnit adUnit) {
    if (killSwitchEngaged()) {
      return null;
    }
    CacheAdUnit cacheAdUnit = mapToCacheAdUnit(adUnit);
    if (cacheAdUnit == null) {
      return null;
    }

    synchronized (cacheLock) {
      if (!isSilencedFor(cacheAdUnit)) {
        fetchForCache(cacheAdUnit, new ContextData() /* TODO */);
      }

      return consumeCachedBid(cacheAdUnit);
    }
  }

  private boolean isSilencedFor(@NonNull CacheAdUnit cacheAdUnit) {
    if (isGlobalSilenceEnabled()) {
      return true;
    }

    synchronized (cacheLock) {
      CdbResponseSlot cdbResponseSlot = cache.peekAdUnit(cacheAdUnit);
      return isBidCurrentlySilent(cdbResponseSlot);
    }
  }

  private void consumeCachedBidIfExpired(@NonNull CacheAdUnit cacheAdUnit) {
    synchronized (cacheLock) {
      CdbResponseSlot cdbResponseSlot = cache.peekAdUnit(cacheAdUnit);
      if (cdbResponseSlot != null && hasBidExpired(cdbResponseSlot)) {
        cache.remove(cacheAdUnit);
        bidLifecycleListener.onBidConsumed(cacheAdUnit, cdbResponseSlot);
      }
    }
  }

  private CdbResponseSlot consumeCachedBid(@NonNull CacheAdUnit cacheAdUnit) {
    synchronized (cacheLock) {
      CdbResponseSlot cdbResponseSlot = cache.peekAdUnit(cacheAdUnit);

      if (cdbResponseSlot != null) {
        boolean isBidSilent = isBidCurrentlySilent(cdbResponseSlot);
        boolean hasBidExpired = hasBidExpired(cdbResponseSlot);

        if (!isBidSilent) {
          cache.remove(cacheAdUnit);
          bidLifecycleListener.onBidConsumed(cacheAdUnit, cdbResponseSlot);
        }

        if (!isBidSilent && !hasBidExpired) {
          return cdbResponseSlot;
        }
      }

      return null;
    }
  }

  void consumeCachedBid(@NonNull CacheAdUnit cacheAdUnit, @NonNull BidListener bidListener) {
    CdbResponseSlot cdbResponseSlot = consumeCachedBid(cacheAdUnit);
    if (cdbResponseSlot != null) {
      bidListener.onBidResponse(cdbResponseSlot);
    } else {
      bidListener.onNoBid();
    }
  }

  /**
   * load data for next time
   */
  private void fetchForCache(@NonNull CacheAdUnit cacheAdUnit, @NonNull ContextData contextData) {
    sendBidRequest(Collections.singletonList(cacheAdUnit), contextData);
  }

  @VisibleForTesting
  void getLiveBidForAdUnit(@NonNull AdUnit adUnit, @NonNull BidListener bidListener) {
    if (killSwitchEngaged()) {
      bidListener.onNoBid();
      return;
    }

    CacheAdUnit cacheAdUnit = mapToCacheAdUnit(adUnit);
    if (cacheAdUnit == null) {
      bidListener.onNoBid();
      return;
    }

    synchronized (cacheLock) {
      consumeCachedBidIfExpired(cacheAdUnit);

      if (isSilencedFor(cacheAdUnit)) {
        consumeCachedBid(cacheAdUnit, bidListener);
      } else {
        liveBidRequestSender.sendLiveBidRequest(
            cacheAdUnit,
            new ContextData(), // TODO
            new LiveCdbCallListener(
                bidListener,
                bidLifecycleListener,
                this,
                cacheAdUnit
            )
        );
      }

      metricSendingQueueConsumer.sendMetricBatch();
    }
  }

  private void sendBidRequest(
      @NonNull List<CacheAdUnit> prefetchCacheAdUnits,
      @NonNull ContextData contextData
  ) {
    if (killSwitchEngaged()) {
      return;
    }

    bidRequestSender.sendBidRequest(prefetchCacheAdUnits, contextData, new CacheOnlyCdbCallListener());
    metricSendingQueueConsumer.sendMetricBatch();
  }

  void setCacheAdUnits(@NonNull List<CdbResponseSlot> slots) {
    synchronized (cacheLock) {
      for (CdbResponseSlot slot : slots) {
        CdbResponseSlot cachedSlot = cache.peekAdUnit(cache.detectCacheAdUnit(slot));
        if (isBidCurrentlySilent(cachedSlot)) {
          // Do not override silence bid that was concurrently cached.
          continue;
        }

        if (slot.isValid()) {
          boolean isImmediateBid = getCpm(slot) > 0 && slot.getTtlInSeconds() == 0;
          if (isImmediateBid) {
            slot.setTtlInSeconds(DEFAULT_TTL_IN_SECONDS);
          }

          cache.add(slot);
          bidLifecycleListener.onBidCached(slot);
        }
      }
    }
  }

  @Nullable
  @VisibleForTesting
  CacheAdUnit mapToCacheAdUnit(@Nullable AdUnit adUnit) {
    CacheAdUnit cacheAdUnit = adUnitMapper.map(adUnit);
    if (cacheAdUnit == null) {
      Log.e(TAG, "Valid AdUnit is required.");
      return null;
    }
    return cacheAdUnit;
  }

  void setTimeToNextCall(int seconds) {
    if (seconds > 0) {
      this.cdbTimeToNextCall.set(clock.getCurrentTimeInMillis() + seconds * 1000);
    }
  }

  boolean isBidCurrentlySilent(@Nullable CdbResponseSlot cdbResponseSlot) {
    if (cdbResponseSlot == null) {
      return false;
    }

    boolean isBidSilent = cdbResponseSlot.getTtlInSeconds() > 0 && getCpm(cdbResponseSlot) == 0;
    return isBidSilent && !hasBidExpired(cdbResponseSlot);
  }

  private boolean hasBidExpired(@NonNull CdbResponseSlot cdbResponseSlot) {
    return cdbResponseSlot.isExpired(clock);
  }

  @VisibleForTesting
  boolean isGlobalSilenceEnabled() {
    return cdbTimeToNextCall.get() > clock.getCurrentTimeInMillis();
  }

  @Override
  public void onApplicationStopped() {
    bidRequestSender.cancelAllPendingTasks();
  }

  /**
   * This method is called back after the "useragent" is fetched
   *
   * @param adUnits list of ad units to prefetch
   */
  public void prefetch(@NonNull List<AdUnit> adUnits) {
    bidRequestSender.sendRemoteConfigRequest(config);

    if (config.isPrefetchOnInitEnabled()) {
      List<List<CacheAdUnit>> requestedAdUnitsChunks = adUnitMapper.mapToChunks(adUnits);

      for (List<CacheAdUnit> requestedAdUnits : requestedAdUnitsChunks) {
        sendBidRequest(requestedAdUnits, new ContextData());
      }
    }
  }

  private boolean killSwitchEngaged() {
    return config.isKillSwitchEnabled();
  }

  private double getCpm(@NonNull CdbResponseSlot cdbResponseSlot) {
    return cdbResponseSlot.getCpmAsNumber() == null ? 0.0 : cdbResponseSlot.getCpmAsNumber();
  }

  /**
   * Implementation specific to listening Cdb calls for updating the cache only
   */
  private class CacheOnlyCdbCallListener extends CdbCallListener {

    public CacheOnlyCdbCallListener() {
      super(bidLifecycleListener, BidManager.this);
    }

    @Override
    public void onCdbResponse(
        @NonNull CdbRequest cdbRequest,
        @NonNull CdbResponse cdbResponse
    ) {
      setCacheAdUnits(cdbResponse.getSlots());
      super.onCdbResponse(cdbRequest, cdbResponse);
    }

    @Override
    public void onTimeBudgetExceeded() {
      // no-op
    }
  }
}
