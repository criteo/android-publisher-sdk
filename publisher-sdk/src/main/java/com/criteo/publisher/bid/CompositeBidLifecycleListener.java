package com.criteo.publisher.bid;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Slot;
import java.util.Arrays;
import java.util.List;

public class CompositeBidLifecycleListener implements BidLifecycleListener {

  @NonNull
  private final List<BidLifecycleListener> delegates;

  public CompositeBidLifecycleListener(@NonNull BidLifecycleListener... delegates) {
    this.delegates = Arrays.asList(delegates);
  }

  @Override
  public void onSdkInitialized() {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onSdkInitialized();
    }
  }

  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onCdbCallStarted(request);
    }
  }

  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onCdbCallFinished(request, response);
    }
  }

  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onCdbCallFailed(request, exception);
    }
  }

  @Override
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull Slot consumedBid) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onBidConsumed(adUnit, consumedBid);
    }
  }
}
