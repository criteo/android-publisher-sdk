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

package com.criteo.publisher.headerbidding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.Bid;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.CdbResponseSlot;
import java.util.List;

public class HeaderBidding {

  private final Logger logger = LoggerFactory.getLogger(HeaderBidding.class);

  @NonNull
  private final List<HeaderBiddingHandler> handlers;

  @NonNull
  private final IntegrationRegistry integrationRegistry;

  public HeaderBidding(
      @NonNull List<HeaderBiddingHandler> handlers,
      @NonNull IntegrationRegistry integrationRegistry
  ) {
    this.handlers = handlers;
    this.integrationRegistry = integrationRegistry;
  }

  public void enrichBid(@Nullable Object object, @Nullable Bid bid) {
    logger.log(AppBiddingLogMessage.onTryingToEnrichAdObjectFromBid(bid));

    if (object != null) {
      for (HeaderBiddingHandler handler : handlers) {
        if (handler.canHandle(object)) {
          integrationRegistry.declare(handler.getIntegration());

          CdbResponseSlot slot = bid == null ? null : bid.consumeSlot();
          handler.cleanPreviousBid(object);

          if (slot == null) {
            logger.log(AppBiddingLogMessage.onAdObjectEnrichedWithNoBid(handler.getIntegration()));
            return;
          }

          handler.enrichBid(object, bid.getAdUnitType(), slot);
          return;
        }
      }
    }

    logger.log(AppBiddingLogMessage.onUnknownAdObjectEnriched(object));
  }

}
