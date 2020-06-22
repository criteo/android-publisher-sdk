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

package com.criteo.publisher.advancednative;

import androidx.annotation.NonNull;
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

  @NonNull
  private final RendererHelper rendererHelper;

  public NativeAdMapper(
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionHelper impressionHelper,
      @NonNull ClickDetection clickDetection,
      @NonNull ClickHelper clickHelper,
      @NonNull AdChoiceOverlay adChoiceOverlay,
      @NonNull RendererHelper rendererHelper
  ) {
    this.visibilityTracker = visibilityTracker;
    this.impressionHelper = impressionHelper;
    this.clickDetection = clickDetection;
    this.clickHelper = clickHelper;
    this.adChoiceOverlay = adChoiceOverlay;
    this.rendererHelper = rendererHelper;
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

    rendererHelper.preloadMedia(nativeAssets.getProduct().getImageUrl());
    rendererHelper.preloadMedia(nativeAssets.getAdvertiserLogoUrl());
    rendererHelper.preloadMedia(nativeAssets.getPrivacyOptOutImageUrl());

    return new CriteoNativeAd(
        nativeAssets,
        visibilityTracker,
        impressionTask,
        clickDetection,
        clickOnProductHandler,
        clickOnAdChoiceHandler,
        adChoiceOverlay,
        renderer,
        rendererHelper
    );
  }

}
