package com.criteo.publisher;

import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AbstractTokenValue;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Slot;
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
    return new BidResponse(price, tokenCache.add(tokenValue, adUnit), true);
  }

  @Nullable
  public DisplayUrlTokenValue getTokenValue(@Nullable BidToken bidToken, @NonNull AdUnitType adUnitType) {
    AbstractTokenValue tokenValue = tokenCache.getTokenValue(bidToken, adUnitType);
    if (!(tokenValue instanceof DisplayUrlTokenValue)) {
      // This should not happen. Tokens are forged with the expected type
      return null;
    }

    return (DisplayUrlTokenValue) tokenValue;
  }

  @Nullable
  public NativeTokenValue getNativeTokenValue(@Nullable BidToken bidToken) {
    AbstractTokenValue tokenValue = tokenCache.getTokenValue(bidToken, CRITEO_CUSTOM_NATIVE);
    if (!(tokenValue instanceof NativeTokenValue)) {
      // This should not happen. Tokens are forged with the expected type
      return null;
    }

    return (NativeTokenValue) tokenValue;
  }

}
