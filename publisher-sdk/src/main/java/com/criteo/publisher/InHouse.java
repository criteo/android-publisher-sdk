package com.criteo.publisher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;

public class InHouse {

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final TokenCache tokenCache;

  @NonNull
  private final Clock clock;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  public InHouse(
      @NonNull BidManager bidManager,
      @NonNull TokenCache tokenCache,
      @NonNull Clock clock,
      @NonNull InterstitialActivityHelper interstitialActivityHelper) {
    this.bidManager = bidManager;
    this.tokenCache = tokenCache;
    this.clock = clock;
    this.interstitialActivityHelper = interstitialActivityHelper;
  }

  @NonNull
  public BidResponse getBidResponse(@Nullable AdUnit adUnit) {
    if (adUnit instanceof InterstitialAdUnit && !interstitialActivityHelper.isAvailable()) {
      return new BidResponse();
    }

    Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    if (slot == null || adUnit == null) {
      return new BidResponse();
    }

    TokenValue tokenValue = new TokenValue(
        slot.getTimeOfDownload(),
        slot.getTtl(),
        slot.getDisplayUrl(),
        clock
    );

    double price = slot.getCpmAsNumber();
    return new BidResponse(price, tokenCache.add(tokenValue, adUnit), true);
  }

  @Nullable
  public TokenValue getTokenValue(@Nullable BidToken bidToken, @NonNull AdUnitType adUnitType) {
    return tokenCache.getTokenValue(bidToken, adUnitType);
  }

}
