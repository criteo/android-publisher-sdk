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

package com.criteo.publisher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import org.jetbrains.annotations.NotNull;

/**
 * Component delivering consumable {@linkplain Bid bids} for publishers.
 *
 * The bids are "consumable" because they can be used only once to display an Ad. Note that {@link Bid} is not called
 * <code>ConsumableBid</code> because it is part of the public API.
 */
public class ConsumableBidLoader {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final Clock clock;

  @NonNull
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;

  public ConsumableBidLoader(
      @NonNull BidManager bidManager,
      @NonNull Clock clock,
      @NonNull RunOnUiThreadExecutor runOnUiThreadExecutor
  ) {
    this.bidManager = bidManager;
    this.clock = clock;
    this.runOnUiThreadExecutor = runOnUiThreadExecutor;
  }

  public void loadBid(
      @NonNull AdUnit adUnit,
      @NonNull ContextData contextData,
      @NonNull BidResponseListener bidResponseListener
  ) {
    bidManager.getBidForAdUnit(adUnit, contextData, new BidListener() {
      @Override
      public void onBidResponse(@NotNull CdbResponseSlot cdbResponseSlot) {
        Bid bid = new Bid(adUnit.getAdUnitType(), clock, cdbResponseSlot);
        responseBid(bid);
      }

      @Override
      public void onNoBid() {
        responseBid(null);
      }

      private void responseBid(@Nullable Bid bid) {
        logger.log(BiddingLogMessage.onConsumableBidLoaded(adUnit, bid));

        // The bid object is used for AppBidding and InHouse.
        // For MoPub AppBidding, it is mandatory to be on the main thread.
        // For InHouse, it is preferable.
        runOnUiThreadExecutor.executeAsync(() -> bidResponseListener.onResponse(bid));
      }
    });
  }

}
