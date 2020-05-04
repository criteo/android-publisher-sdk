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

  public NativeAdMapper(
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionHelper impressionHelper,
      @NonNull ClickDetection clickDetection,
      @NonNull ClickHelper clickHelper
  ) {
    this.visibilityTracker = visibilityTracker;
    this.impressionHelper = impressionHelper;
    this.clickDetection = clickDetection;
    this.clickHelper = clickHelper;
  }

  @NonNull
  CriteoNativeAd map(
      @NonNull NativeAssets nativeAssets,
      @NonNull WeakReference<CriteoNativeAdListener> listenerRef
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

    return new CriteoNativeAd(
        nativeAssets,
        visibilityTracker,
        impressionTask,
        clickDetection,
        clickOnProductHandler
    );
  }

}
