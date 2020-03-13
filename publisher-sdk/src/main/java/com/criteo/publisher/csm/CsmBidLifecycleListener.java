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

public class CsmBidLifecycleListener implements BidLifecycleListener {

  @NonNull
  private final MetricRepository repository;

  @NonNull
  private final Clock clock;

  public CsmBidLifecycleListener(
      @NonNull MetricRepository repository,
      @NonNull Clock clock
  ) {
    this.repository = repository;
    this.clock = clock;
  }

  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    long currentTimeInMillis = clock.getCurrentTimeInMillis();

    updateByCdbRequestIds(request, new MetricUpdater() {
      @Override
      public void update(@NonNull MetricBuilder builder) {
        builder.setCdbCallStartAbsolute(currentTimeInMillis);
      }
    });
  }

  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    long currentTimeInMillis = clock.getCurrentTimeInMillis();

    updateByCdbRequestIds(request, new MetricUpdater() {
      @Override
      public void update(@NonNull MetricBuilder builder) {
        builder.setCdbCallEndAbsolute(currentTimeInMillis);
      }
    });
  }

  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {

  }

  @Override
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull Slot consumedBid) {

  }

  private void updateByCdbRequestIds(@NonNull CdbRequest request, @NonNull MetricUpdater updater) {
    for (CdbRequestSlot requestSlot : request.getSlots()) {
      repository.updateById(requestSlot.getImpressionId(), updater);
    }
  }

}
