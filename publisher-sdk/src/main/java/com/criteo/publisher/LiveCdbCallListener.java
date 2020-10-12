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

import static java.util.Collections.singletonList;

import androidx.annotation.NonNull;
import com.criteo.publisher.annotation.Internal;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.util.PreconditionsUtil;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation specific to listening Cdb calls for serving a live request
 */
@Internal
public class LiveCdbCallListener extends CdbCallListener {

  private final BidListener bidListener;
  private final BidManager bidManager;
  private final CacheAdUnit cacheAdUnit;
  private final BidLifecycleListener bidLifecycleListener;
  private final AtomicBoolean isListenerTriggered = new AtomicBoolean(false);

  public LiveCdbCallListener(
      @NonNull BidListener bidListener,
      @NonNull BidLifecycleListener bidLifecycleListener,
      @NonNull BidManager bidManager,
      @NonNull CacheAdUnit cacheAdUnit
  ) {
    super(bidLifecycleListener, bidManager);
    this.bidListener = bidListener;
    this.bidLifecycleListener = bidLifecycleListener;
    this.bidManager = bidManager;
    this.cacheAdUnit = cacheAdUnit;
  }

  /**
   * Triggered when a response is fetched before the expiration of the network timeout. Following
   * this, two things can happen:
   * <p>
   * 1. If {@link LiveCdbCallListener#onTimeBudgetExceeded()}  hasn't been triggered yet (on a
   * separate thread), a bid response is returned to the caller via {@link
   * BidListener#onBidResponse(CdbResponseSlot)} unless the {@link CdbResponseSlot} has been
   * silenced, in which case {@link BidListener#onNoBid()} is triggered instead.
   * <p>
   * 2. If {@link LiveCdbCallListener#onTimeBudgetExceeded()} has been triggered, then either {@link
   * BidListener#onNoBid()} or {@link BidListener#onBidResponse(CdbResponseSlot)} were already
   * triggered. The only action that needs to be taken here is to cache the {@link
   * CdbResponseSlot}.
   */
  @Override
  public void onCdbResponse(
      @NonNull CdbRequest cdbRequest,
      @NonNull CdbResponse cdbResponse
  ) {
    super.onCdbResponse(cdbRequest, cdbResponse);

    if (cdbResponse.getSlots().size() > 1) {
      PreconditionsUtil.throwOrLog(new IllegalStateException(
          "During a live request, only one bid will be fetched at a time."));
    }
    if (isListenerTriggered.compareAndSet(false, true)) {
      if (cdbResponse.getSlots().size() == 1) {
        serveBidResponseIfPossible(cdbResponse.getSlots().get(0));
      } else {
        bidListener.onNoBid();
      }
    } else {
      bidManager.setCacheAdUnits(cdbResponse.getSlots());
    }
  }

  private void serveBidResponseIfPossible(@NonNull CdbResponseSlot cdbResponseSlot) {
    if (bidManager.isBidCurrentlySilent(cdbResponseSlot)) {
      bidManager.setCacheAdUnits(singletonList(cdbResponseSlot));
      bidListener.onNoBid();
    } else if (cdbResponseSlot.isValid()) {
      bidListener.onBidResponse(cdbResponseSlot);
      bidLifecycleListener.onBidConsumed(cacheAdUnit, cdbResponseSlot);
    } else {
      bidListener.onNoBid();
    }
  }

  /**
   * Triggered when an error happens while fetching a bid. A bid is returned only if it is available in the cache,
   * unless a no-bid was already returned by {@link LiveCdbCallListener#onTimeBudgetExceeded()} on a separate thread.
   */
  @Override
  public void onCdbError(@NonNull CdbRequest cdbRequest, @NonNull Exception exception) {
    super.onCdbError(cdbRequest, exception);
    onTimeBudgetExceeded();
  }

  /**
   * If the time-budget is exceeded, a bid is returned only if it is available in the cache.
   * Otherwise {@link BidListener#onNoBid()} is triggered
   */
  @Override
  public void onTimeBudgetExceeded() {
    if (isListenerTriggered.compareAndSet(false, true)) {
      bidManager.consumeCachedBid(cacheAdUnit, bidListener);
    }
  }
}
