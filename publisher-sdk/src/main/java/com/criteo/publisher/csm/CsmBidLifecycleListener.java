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

  @Override
  public void onSdkInitialized() {
    sendingQueueProducer.pushAllInQueue(repository);
  }

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
            builder.setImpressionId(impressionId);
          }
        }
      });
    }

    if (shouldPushInQueue) {
      sendingQueueProducer.pushAllReadyToSendInQueue(repository);
    }
  }

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
    long currentTimeInMillis = clock.getCurrentTimeInMillis();

    updateByCdbRequestIds(request, new MetricUpdater() {
      @Override
      public void update(@NonNull Metric.Builder builder) {
        builder.setCdbCallTimeoutTimestamp(currentTimeInMillis);
        builder.setReadyToSend(true);
      }
    });
  }

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
