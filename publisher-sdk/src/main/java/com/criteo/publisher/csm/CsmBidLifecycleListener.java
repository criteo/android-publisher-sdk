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

package com.criteo.publisher.csm;

import androidx.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.csm.MetricRepository.MetricUpdater;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestSlot;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import java.io.InterruptedIOException;
import java.util.concurrent.Executor;

/**
 * Update metrics files accordingly to received events.
 * <p>
 * This follows specifications given by <a href="https://go.crto.in/publisher-sdk-csm">Client Side
 * Metrics</a>.
 */
public class CsmBidLifecycleListener implements BidLifecycleListener {

  @NonNull
  private final MetricRepository repository;

  @NonNull
  private final MetricSendingQueueProducer sendingQueueProducer;

  @NonNull
  private final Clock clock;

  @NonNull
  private final Config config;

  @NonNull
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final Executor executor;

  public CsmBidLifecycleListener(
      @NonNull MetricRepository repository,
      @NonNull MetricSendingQueueProducer sendingQueueProducer,
      @NonNull Clock clock,
      @NonNull Config config,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull Executor executor
  ) {
    this.repository = repository;
    this.sendingQueueProducer = sendingQueueProducer;
    this.clock = clock;
    this.config = config;
    this.userPrivacyUtil = userPrivacyUtil;
    this.executor = executor;
  }

  /**
   * SDK initialization is caused either by a fresh or restart of the application. As a consequence,
   * the in-memory bid-cache is lost and the metrics associated to the previously cached bids will
   * never be updated again. In this case, all previously stored metrics are moved to the sending
   * queue.
   */
  @Override
  public void onSdkInitialized() {
    if (isCsmDisabled()) {
      return;
    }

    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        sendingQueueProducer.pushAllInQueue(repository);
      }
    });
  }

  /**
   * On CDB call start, each requested slot is tracked by a new metric. The metrics marks the
   * timestamp of this event and wait for further updates.
   *
   * @param request Request sent to CDB
   */
  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    if (isCsmDisabled()) {
      return;
    }

    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        long currentTimeInMillis = clock.getCurrentTimeInMillis();

        updateByCdbRequestIds(request, builder -> {
          builder.setRequestGroupId(request.getId());
          builder.setCdbCallStartTimestamp(currentTimeInMillis);
          builder.setProfileId(request.getProfileId());
        });
      }
    });
  }

  /**
   * When the CDB call ends successfully, metrics corresponding to requested slots are updated
   * accordingly to the response.
   * <p>
   * If there is no response for a slot, then it is a no bid. The metric marks the timestamp of this
   * event and, as no consumption of this no-bid is expected, the metric is tagged as finished and
   * ready to send.
   * <p>
   * If there is a matching invalid slot, then it is considered as an error. The metric is not
   * longer updated and is flagged as ready to send.
   * <p>
   * If there is a matching valid slot, then it is a consumable bid. The metric marks the timestamp
   * of this event, and waits for further updates (via consumption).
   *
   * @param request Request that was sent to CDB
   * @param response Response coming from CDB
   */
  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    if (isCsmDisabled()) {
      return;
    }

    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        long currentTimeInMillis = clock.getCurrentTimeInMillis();

        for (CdbRequestSlot requestSlot : request.getSlots()) {
          String impressionId = requestSlot.getImpressionId();
          CdbResponseSlot responseSlot = response.getSlotByImpressionId(impressionId);
          boolean isNoBid = responseSlot == null;
          boolean isInvalidBid = responseSlot != null && !responseSlot.isValid();

          repository.addOrUpdateById(impressionId, builder -> {
            if (isNoBid) {
              builder.setCdbCallEndTimestamp(currentTimeInMillis);
              builder.setReadyToSend(true);
            } else if (isInvalidBid) {
              builder.setReadyToSend(true);
            } else /* if isValidBid */ {
              builder.setCdbCallEndTimestamp(currentTimeInMillis);
              builder.setZoneId(responseSlot.getZoneId());
            }
          });

          if (isNoBid || isInvalidBid) {
            sendingQueueProducer.pushInQueue(repository, impressionId);
          }
        }
      }
    });
  }

  /**
   * On CDB call failed, metrics corresponding to the requested slots are updated.
   * <p>
   * If the failure is a timeout, then all metrics are flagged as having a timeout.
   * <p>
   * Then, since no further updates are expected, all metrics are flagged as ready to send.
   *
   * @param request Request that was sent to CDB
   * @param exception Exception representing the failure of the call
   */
  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {
    if (isCsmDisabled()) {
      return;
    }

    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        // InterruptedIOException was thrown in older versions of Okio
        // See https://github.com/square/okhttp/blob/master/docs/changelog_2x.md
        boolean isTimeout = exception instanceof InterruptedIOException;

        if (isTimeout) {
          onCdbCallTimeout(request);
        } else {
          onCdbCallNetworkError(request);
        }

        for (CdbRequestSlot slot : request.getSlots()) {
          String impressionId = slot.getImpressionId();
          sendingQueueProducer.pushInQueue(repository, impressionId);
        }
      }
    });
  }

  private void onCdbCallNetworkError(CdbRequest request) {
    updateByCdbRequestIds(request, builder -> builder.setReadyToSend(true));
  }

  private void onCdbCallTimeout(@NonNull CdbRequest request) {
    updateByCdbRequestIds(request, builder -> {
      builder.setCdbCallTimeout(true);
      builder.setReadyToSend(true);
    });
  }

  /**
   * On bid consumption, the metric associated to the bid is updated.
   * <p>
   * If the bid has not expired, then the bid managed to go from CDB to the user. The metric marks
   * the timestamp of this event.
   * <p>
   * Since this is the end of the bid lifecycle, the metric does not expect further updates and is
   * flagged as ready to send.
   *
   * @param adUnit ad unit representing the bid
   * @param consumedBid bid that was consumed
   */
  @Override
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull CdbResponseSlot consumedBid) {
    if (isCsmDisabled()) {
      return;
    }

    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        String impressionId = consumedBid.getImpressionId();
        if (impressionId == null) {
          return;
        }

        boolean isNotExpired = !consumedBid.isExpired(clock);
        long currentTimeInMillis = clock.getCurrentTimeInMillis();

        repository.addOrUpdateById(impressionId, builder -> {
          if (isNotExpired) {
            builder.setElapsedTimestamp(currentTimeInMillis);
          }

          builder.setReadyToSend(true);
        });

        sendingQueueProducer.pushInQueue(repository, impressionId);
      }
    });
  }

  @Override
  public void onBidCached(@NonNull CdbResponseSlot bidCached) {
    if (isCsmDisabled()) {
      return;
    }

    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        String impressionId = bidCached.getImpressionId();
        if (impressionId == null) {
          return;
        }

        if (!bidCached.isValid()) {
          return;
        }

        repository.addOrUpdateById(impressionId, builder -> builder.setCachedBidUsed(true));
      }
    });
  }

  private void updateByCdbRequestIds(@NonNull CdbRequest request, @NonNull MetricUpdater updater) {
    for (CdbRequestSlot requestSlot : request.getSlots()) {
      repository.addOrUpdateById(requestSlot.getImpressionId(), updater);
    }
  }

  private boolean isCsmDisabled() {
    return !config.isCsmEnabled() || userPrivacyUtil.isCsmDisallowed();
  }
}
