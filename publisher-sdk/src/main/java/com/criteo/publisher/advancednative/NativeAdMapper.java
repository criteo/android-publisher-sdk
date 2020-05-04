package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.nativeads.NativeAssets;
import java.lang.ref.WeakReference;

public class NativeAdMapper {

  @NonNull
  private final VisibilityTracker visibilityTracker;

  @NonNull
  private final ImpressionHelper impressionHelper;

  public NativeAdMapper(
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionHelper impressionHelper
  ) {
    this.visibilityTracker = visibilityTracker;
    this.impressionHelper = impressionHelper;
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

    return new CriteoNativeAd(nativeAssets, visibilityTracker, impressionTask);
  }

}
