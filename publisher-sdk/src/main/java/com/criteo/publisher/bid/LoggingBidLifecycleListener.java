package com.criteo.publisher.bid;

import android.support.annotation.NonNull;
import android.util.Log;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.model.CacheAdUnit;
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
    log("onCdbCallStarted: %s", request);
  }

  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    log("onCdbCallFinished: %s", response);
  }

  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {
    if (isEnabled()) {
      Log.d(TAG, "onCdbCallFailed", exception);
    }
  }

  @Override
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull Slot consumedBid) {
    log("onBidConsumed: %s", consumedBid);
  }

  private void log(String format, Object... args) {
    if (isEnabled()) {
      Log.d(TAG, String.format(format, args));
    }
  }

  private boolean isEnabled() {
    return loggingUtil.isLoggingEnabled();
  }
}
