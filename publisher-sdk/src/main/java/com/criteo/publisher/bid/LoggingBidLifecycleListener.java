package com.criteo.publisher.bid;

import android.support.annotation.NonNull;
import android.util.Log;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Slot;

/**
 * Listener that logs debug messages given the different steps of a bid lifecycle.
 */
public class LoggingBidLifecycleListener implements BidLifecycleListener {

  private static final String TAG = LoggingBidLifecycleListener.class.getSimpleName();

  @NonNull
  private final LoggingUtil loggingUtil;

  public LoggingBidLifecycleListener(@NonNull LoggingUtil loggingUtil) {
    this.loggingUtil = loggingUtil;
  }

  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    // TODO
  }

  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    if (!isEnabled()) {
      return;
    }

    StringBuilder builder = new StringBuilder();
    for (Slot slot : response.getSlots()) {
      builder.append(slot.toString());
      builder.append("\n");
    }
    Log.d(TAG, builder.toString());
  }

  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {
    // TODO
  }

  private boolean isEnabled() {
    return loggingUtil.isLoggingEnabled();
  }
}
