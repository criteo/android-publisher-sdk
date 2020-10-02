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
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.InterstitialAdUnit;

public class InHouse {

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final Clock clock;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  @NonNull
  private final IntegrationRegistry integrationRegistry;

  public InHouse(
      @NonNull BidManager bidManager,
      @NonNull Clock clock,
      @NonNull InterstitialActivityHelper interstitialActivityHelper,
      @NonNull IntegrationRegistry integrationRegistry
  ) {
    this.bidManager = bidManager;
    this.clock = clock;
    this.interstitialActivityHelper = interstitialActivityHelper;
    this.integrationRegistry = integrationRegistry;
  }

  public void loadBidResponse(
      @NonNull AdUnit adUnit,
      @NonNull BidResponseListener bidResponseListener
  ) {
    integrationRegistry.declare(Integration.IN_HOUSE);

    if (adUnit instanceof InterstitialAdUnit && !interstitialActivityHelper.isAvailable()) {
      bidResponseListener.onResponse(null);
      return;
    }

    CdbResponseSlot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    if (slot == null) {
      bidResponseListener.onResponse(null);
      return;
    }

    BidResponse bidResponse = new BidResponse(adUnit.getAdUnitType(), clock, slot);
    bidResponseListener.onResponse(bidResponse);
  }

}
