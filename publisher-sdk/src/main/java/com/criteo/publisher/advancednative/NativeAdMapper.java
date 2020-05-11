package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.nativeads.NativeAssets;
import java.lang.ref.WeakReference;

public class NativeAdMapper {

  @NonNull
  private final VisibilityTracker visibilityTracker;

  @NonNull
  private final ImpressionHelper impressionHelper;

  @NonNull
  private final ClickDetection clickDetection;

  @NonNull
  private final ClickHelper clickHelper;

  @NonNull
  private final AdChoiceOverlay adChoiceOverlay;

  public NativeAdMapper(
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionHelper impressionHelper,
      @NonNull ClickDetection clickDetection,
      @NonNull ClickHelper clickHelper,
      @NonNull AdChoiceOverlay adChoiceOverlay
  ) {
    this.visibilityTracker = visibilityTracker;
    this.impressionHelper = impressionHelper;
    this.clickDetection = clickDetection;
    this.clickHelper = clickHelper;
    this.adChoiceOverlay = adChoiceOverlay;
  }

  @NonNull
  CriteoNativeAd map(
      @NonNull NativeAssets nativeAssets,
      @NonNull WeakReference<CriteoNativeAdListener> listenerRef,
      @NonNull CriteoNativeRenderer renderer
  ) {
    ImpressionTask impressionTask = new ImpressionTask(
        nativeAssets.getImpressionPixels(),
        listenerRef,
        impressionHelper);

    NativeViewClickHandler clickOnProductHandler = new AdViewClickHandler(
        nativeAssets.getProduct().getClickUrl(),
        listenerRef,
        clickHelper
    );

    NativeViewClickHandler clickOnAdChoiceHandler = new AdChoiceClickHandler(
        nativeAssets.getPrivacyOptOutClickUrl(),
        listenerRef,
        clickHelper
    );

    return new CriteoNativeAd(
        nativeAssets,
        visibilityTracker,
        impressionTask,
        clickDetection,
        clickOnProductHandler,
        clickOnAdChoiceHandler,
        adChoiceOverlay,
        renderer
    );
  }

}
