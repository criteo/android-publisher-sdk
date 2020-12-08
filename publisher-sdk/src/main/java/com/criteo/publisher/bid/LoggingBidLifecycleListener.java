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

package com.criteo.publisher.bid;

import androidx.annotation.NonNull;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.logging.RemoteLogSendingQueueConsumer;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.CdbResponseSlot;

/**
 * Listener that logs debug messages given the different steps of a bid lifecycle.
 */
public class LoggingBidLifecycleListener implements BidLifecycleListener {

  private final Logger logger = LoggerFactory.getLogger(LoggingBidLifecycleListener.class);

  @NonNull
  private final RemoteLogSendingQueueConsumer remoteLogSendingQueueConsumer;

  public LoggingBidLifecycleListener(@NonNull RemoteLogSendingQueueConsumer remoteLogSendingQueueConsumer) {
    this.remoteLogSendingQueueConsumer = remoteLogSendingQueueConsumer;
  }

  @Override
  public void onSdkInitialized() {
    logger.debug("onSdkInitialized");
    remoteLogSendingQueueConsumer.sendRemoteLogBatch();
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
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull CdbResponseSlot consumedBid) {
    logger.debug("onBidConsumed: %s", consumedBid);
  }

  @Override
  public void onBidCached(@NonNull CdbResponseSlot cachedBid) {
    logger.debug("onBidCached: %s", cachedBid);
  }

}
