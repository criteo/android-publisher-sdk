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

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final Clock clock;

  public ConsumableBidLoader(
      @NonNull BidManager bidManager,
      @NonNull Clock clock
  ) {
    this.bidManager = bidManager;
    this.clock = clock;
  }

  public void loadBid(
      @NonNull AdUnit adUnit,
      @NonNull BidResponseListener bidResponseListener
  ) {
    bidManager.getBidForAdUnit(adUnit, new BidListener() {
      @Override
      public void onBidResponse(@NotNull CdbResponseSlot cdbResponseSlot) {
        Bid bid = new Bid(adUnit.getAdUnitType(), clock, cdbResponseSlot);
        bidResponseListener.onResponse(bid);
      }

      @Override
      public void onNoBid() {
        bidResponseListener.onResponse(null);
      }
    });
  }

}
