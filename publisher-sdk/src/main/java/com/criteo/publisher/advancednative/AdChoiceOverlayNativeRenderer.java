package com.criteo.publisher.advancednative;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;

@Keep
class AdChoiceOverlayNativeRenderer implements CriteoNativeRenderer {

  @NonNull
  private final CriteoNativeRenderer delegate;

  @NonNull
  private final AdChoiceOverlay adChoiceOverlay;

  /**
   * Used by MoPub Mediation adapter because it has no access to the dependency provider.
   *
   * As this constructor eagerly accesses the dependency provider, an exception will be thrown if
   * the publisher did not call the SDK initialisation (but with a fancy message indicating how to
   * fix the issue).
   */
  AdChoiceOverlayNativeRenderer(@NonNull CriteoNativeRenderer delegate) {
    this(delegate, DependencyProvider.getInstance().provideAdChoiceOverlay());
  }

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
