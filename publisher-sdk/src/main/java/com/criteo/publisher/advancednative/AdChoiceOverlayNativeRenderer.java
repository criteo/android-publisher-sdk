package com.criteo.publisher.advancednative;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

class AdChoiceOverlayNativeRenderer implements CriteoNativeRenderer {

  @NonNull
  private final CriteoNativeRenderer delegate;

  @NonNull
  private final AdChoiceOverlay adChoiceOverlay;

  AdChoiceOverlayNativeRenderer(
      @NonNull CriteoNativeRenderer delegate,
      @NonNull AdChoiceOverlay adChoiceOverlay
  ) {
    this.delegate = delegate;
    this.adChoiceOverlay = adChoiceOverlay;
  }

  @NonNull
  @Override
  public View createNativeView(
      @NonNull Context context,
      @Nullable ViewGroup parent
  ) {
    return adChoiceOverlay.addOverlay(delegate.createNativeView(context, parent));
  }

  @Override
  public void renderNativeView(
      @NonNull RendererHelper helper,
      @NonNull View nativeView,
      @NonNull CriteoNativeAd nativeAd
  ) {
    View delegateView = adChoiceOverlay.getInitialView(nativeView);
    if (delegateView != null) {
      delegate.renderNativeView(helper, delegateView, nativeAd);
    }
  }
}
