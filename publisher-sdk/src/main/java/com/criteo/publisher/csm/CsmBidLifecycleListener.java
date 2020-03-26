package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.csm.MetricRepository.MetricUpdater;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestSlot;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Slot;
import java.net.SocketTimeoutException;

/**
 * Update metrics files accordingly to received events.
 * <p>
 * This follows specifications given by <a href="https://confluence.criteois.com/display/PUBSDK/Publisher+SDK+-+Client+Side+Metrics">Client
 * Side Metrics</a>.
 */
public class CsmBidLifecycleListener implements BidLifecycleListener {

  @NonNull
  private final MetricRepository repository;

  @NonNull
  private final MetricSendingQueueProducer sendingQueueProducer;

  @NonNull
  private final Clock clock;

  public CsmBidLifecycleListener(
      @NonNull MetricRepository repository,
      @NonNull MetricSendingQueueProducer sendingQueueProducer,
      @NonNull Clock clock
  ) {
    this.repository = repository;
    this.sendingQueueProducer = sendingQueueProducer;
    this.clock = clock;
  }

  /**
   * SDK initialization is caused either by a fresh or restart of the application. As a consequence,
   * the in-memory bid-cache is lost and the metrics associated to the previously cached bids will
   * never be updated again. In this case, all previously stored metrics are moved to the sending
   * queue.
   */
  @Override
  public void onSdkInitialized() {
    sendingQueueProducer.pushAllInQueue(repository);
  }

  /**
   * On CDB call start, each requested slot is tracked by a new metric. The metrics marks the
   * timestamp of this event and wait for further updates.
   *
   * @param request Request sent to CDB
   */
  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    long currentTimeInMillis = clock.getCurrentTimeInMillis();

    updateByCdbRequestIds(request, new MetricUpdater() {
      @Override
      public void update(@NonNull Metric.Builder builder) {
        builder.setCdbCallStartTimestamp(currentTimeInMillis);
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
    boolean shouldPushInQueue = false;
    long currentTimeInMillis = clock.getCurrentTimeInMillis();

    for (CdbRequestSlot requestSlot : request.getSlots()) {
      String impressionId = requestSlot.getImpressionId();
      Slot responseSlot = response.getSlotByImpressionId(impressionId);
      boolean isNoBid = responseSlot == null;
      boolean isInvalidBid = responseSlot != null && !responseSlot.isValid();

      if (isNoBid || isInvalidBid) {
        shouldPushInQueue = true;
      }

      repository.updateById(impressionId, new MetricUpdater() {
        @Override
        public void update(@NonNull Metric.Builder builder) {
          if (isNoBid) {
            builder.setCdbCallEndTimestamp(currentTimeInMillis);
            builder.setReadyToSend(true);
          } else if (isInvalidBid) {
            builder.setReadyToSend(true);
          } else /* if isValidBid */ {
            builder.setCdbCallEndTimestamp(currentTimeInMillis);
            builder.setCachedBidUsed(true);
            builder.setImpressionId(impressionId);
          }
        }
      });
    }

    if (shouldPushInQueue) {
      sendingQueueProducer.pushAllReadyToSendInQueue(repository);
    }
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
    boolean isTimeout = exception instanceof SocketTimeoutException;
    if (isTimeout) {
      onCdbCallTimeout(request);
    } else {
      onCdbCallNetworkError(request);
    }

    sendingQueueProducer.pushAllReadyToSendInQueue(repository);
  }

  private void onCdbCallNetworkError(CdbRequest request) {
    updateByCdbRequestIds(request, new MetricUpdater() {
      @Override
      public void update(@NonNull Metric.Builder builder) {
        builder.setReadyToSend(true);
      }
    });
  }

  private void onCdbCallTimeout(@NonNull CdbRequest request) {
    updateByCdbRequestIds(request, new MetricUpdater() {
      @Override
      public void update(@NonNull Metric.Builder builder) {
        builder.setCdbCallTimeout(true);
        builder.setReadyToSend(true);
      }
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
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull Slot consumedBid) {
    String impressionId = consumedBid.getImpressionId();
    if (impressionId == null) {
      return;
    }

    boolean isNotExpired = !consumedBid.isExpired(clock);
    long currentTimeInMillis = clock.getCurrentTimeInMillis();

    repository.updateById(impressionId, new MetricUpdater() {
      @Override
      public void update(@NonNull Metric.Builder builder) {
        if (isNotExpired) {
          builder.setElapsedTimestamp(currentTimeInMillis);
        }

        builder.setReadyToSend(true);
      }
    });

    sendingQueueProducer.pushAllReadyToSendInQueue(repository);
  }

  private void updateByCdbRequestIds(@NonNull CdbRequest request, @NonNull MetricUpdater updater) {
    for (CdbRequestSlot requestSlot : request.getSlots()) {
      repository.updateById(requestSlot.getImpressionId(), updater);
    }
  }

}
