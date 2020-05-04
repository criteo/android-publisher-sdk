package com.criteo.publisher.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.util.ReflectionUtil;

public class MoPubHeaderBidding implements HeaderBiddingHandler{

  private static final String MOPUB_ADVIEW_CLASS = "com.mopub.mobileads.MoPubView";
  private static final String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  public boolean canHandle(@NonNull Object object) {
    return object.getClass().getName().equals(MOPUB_ADVIEW_CLASS)
        || object.getClass().getName().equals(MOPUB_INTERSTITIAL_CLASS);
  }

  @Override
  public void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit, @NonNull Slot slot) {
    if (!canHandle(object)) {
      return;
    }

    StringBuilder keywords = new StringBuilder();
    Object existingKeywords = ReflectionUtil.callMethodOnObject(object, "getKeywords");
    if (existingKeywords != null) {
      keywords.append(existingKeywords);
      keywords.append(",");
    }
    keywords.append(CRT_CPM);
    keywords.append(":");
    keywords.append(slot.getCpm());
    keywords.append(",");
    keywords.append(CRT_DISPLAY_URL);
    keywords.append(":");
    keywords.append(slot.getDisplayUrl());

    if (adUnit instanceof BannerAdUnit) {
      keywords.append(",");
      keywords.append(CRT_SIZE);
      keywords.append(":");
      keywords.append(slot.getWidth()).append("x").append(slot.getHeight());
    }

    ReflectionUtil.callMethodOnObject(object, "setKeywords", keywords.toString());
  }

}
