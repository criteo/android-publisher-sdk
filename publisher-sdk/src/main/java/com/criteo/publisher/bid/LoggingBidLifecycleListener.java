package com.criteo.publisher.bid;

import androidx.annotation.NonNull;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Slot;

/**
 * Listener that logs debug messages given the different steps of a bid lifecycle.
 */
public class LoggingBidLifecycleListener implements BidLifecycleListener {

  private final Logger logger = LoggerFactory.getLogger(LoggingBidLifecycleListener.class);

  @Override
  public void onSdkInitialized() {
    logger.debug("onSdkInitialized");
  }

  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    logger.debug("onCdbCallStarted: %s", request);
  }

  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    logger.debug("onCdbCallFinished: %s", response);
  }

  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {
    logger.debug("onCdbCallFailed", exception);
  }

  @Override
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull Slot consumedBid) {
    logger.debug("onBidConsumed: %s", consumedBid);
  }

}
