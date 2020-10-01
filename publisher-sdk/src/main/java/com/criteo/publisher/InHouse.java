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

import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AbstractTokenValue;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeTokenValue;
import com.criteo.publisher.util.AdUnitType;

public class InHouse {

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final TokenCache tokenCache;

  @NonNull
  private final Clock clock;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  @NonNull
  private final IntegrationRegistry integrationRegistry;

  public InHouse(
      @NonNull BidManager bidManager,
      @NonNull TokenCache tokenCache,
      @NonNull Clock clock,
      @NonNull InterstitialActivityHelper interstitialActivityHelper,
      @NonNull IntegrationRegistry integrationRegistry
  ) {
    this.bidManager = bidManager;
    this.tokenCache = tokenCache;
    this.clock = clock;
    this.interstitialActivityHelper = interstitialActivityHelper;
    this.integrationRegistry = integrationRegistry;
  }

  @NonNull
  public BidResponse getBidResponse(@Nullable AdUnit adUnit) {
    integrationRegistry.declare(Integration.IN_HOUSE);

    if (adUnit instanceof InterstitialAdUnit && !interstitialActivityHelper.isAvailable()) {
      return BidResponse.NO_BID;
    }

    CdbResponseSlot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    if (slot == null || adUnit == null) {
      return BidResponse.NO_BID;
    }

    AbstractTokenValue tokenValue;

    if (slot.getNativeAssets() != null) {
      tokenValue = new NativeTokenValue(
          slot.getNativeAssets(),
          slot,
          clock
      );
    } else {
      tokenValue = new DisplayUrlTokenValue(
          slot.getDisplayUrl(),
          slot,
          clock
      );
    }

    double price = slot.getCpmAsNumber();
    BidResponse bidResponse = new BidResponse(price, true, adUnit.getAdUnitType());
    tokenCache.add(bidResponse, tokenValue);
    return bidResponse;
  }

  @Nullable
  public String getDisplayUrl(@NonNull BidResponse bidResponse, @NonNull AdUnitType adUnitType) {
    AbstractTokenValue tokenValue = tokenCache.getTokenValue(bidResponse, adUnitType);
    if (!(tokenValue instanceof DisplayUrlTokenValue)) {
      // This should not happen. Tokens are forged with the expected type
      return null;
    }

    return ((DisplayUrlTokenValue) tokenValue).getDisplayUrl();
  }

  @Nullable
  public NativeAssets getNativeAssets(@NonNull BidResponse bidResponse) {
    AbstractTokenValue tokenValue = tokenCache.getTokenValue(bidResponse, CRITEO_CUSTOM_NATIVE);
    if (!(tokenValue instanceof NativeTokenValue)) {
      // This should not happen. Tokens are forged with the expected type
      return null;
    }

    return ((NativeTokenValue) tokenValue).getNativeAssets();
  }

}
