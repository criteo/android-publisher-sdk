package com.criteo.publisher.advancednative;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Helper class giving package access to {@link CriteoNativeAd}.
 * This is only temporary to allow demo of Advance Native in test app while it is incubating.
 */
@Deprecated
public class CriteoNativeAdHelper {

  @SuppressLint("VisibleForTests")
  public static void watchForImpression(@NonNull CriteoNativeAd nativeAd, @NonNull View nativeView) {
    nativeAd.watchForImpression(nativeView);
  }

  @SuppressLint("VisibleForTests")
  public static void setProductClickableView(@NonNull CriteoNativeAd nativeAd, @NonNull View nativeView) {
    nativeAd.setProductClickableView(nativeView);
  }

  @SuppressLint("VisibleForTests")
  public static void setAdChoiceClickableView(@NonNull CriteoNativeAd nativeAd, @NonNull View adChoiceView) {
    nativeAd.setAdChoiceClickableView(adChoiceView);
  }

}
