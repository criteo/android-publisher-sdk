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

import static com.criteo.publisher.annotation.Internal.ADMOB_ADAPTER;
import static com.criteo.publisher.annotation.Internal.MOPUB_ADAPTER;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.annotation.Internal;
import com.criteo.publisher.model.nativeads.NativeAssets;

@Keep
public class CriteoNativeAd {

  @NonNull
  private final NativeAssets assets;

  @NonNull
  private final VisibilityTracker visibilityTracker;

  @NonNull
  private final ImpressionTask impressionTask;

  @NonNull
  private final ClickDetection clickDetection;

  @NonNull
  private final NativeViewClickHandler clickOnProductHandler;

  @NonNull
  private final NativeViewClickHandler clickOnAdChoiceHandler;

  @NonNull
  private final AdChoiceOverlay adChoiceOverlay;

  @NonNull
  private CriteoNativeRenderer renderer;

  @NonNull
  private final RendererHelper rendererHelper;

  public CriteoNativeAd(
      @NonNull NativeAssets assets,
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionTask impressionTask,
      @NonNull ClickDetection clickDetection,
      @NonNull NativeViewClickHandler clickOnProductHandler,
      @NonNull NativeViewClickHandler clickOnAdChoiceHandler,
      @NonNull AdChoiceOverlay adChoiceOverlay,
      @NonNull CriteoNativeRenderer renderer,
      @NonNull RendererHelper rendererHelper
  ) {
    this.assets = assets;
    this.visibilityTracker = visibilityTracker;
    this.impressionTask = impressionTask;
    this.clickDetection = clickDetection;
    this.clickOnProductHandler = clickOnProductHandler;
    this.clickOnAdChoiceHandler = clickOnAdChoiceHandler;
    this.adChoiceOverlay = adChoiceOverlay;
    this.renderer = renderer;
    this.rendererHelper = rendererHelper;
  }

  @NonNull
  public String getTitle() {
    return assets.getProduct().getTitle();
  }

  @NonNull
  public String getDescription() {
    return assets.getProduct().getDescription();
  }

  @NonNull
  public String getPrice() {
    return assets.getProduct().getPrice();
  }

  @NonNull
  public String getCallToAction() {
    return assets.getProduct().getCallToAction();
  }

  @NonNull
  public CriteoMedia getProductMedia() {
    return CriteoMedia.create(assets.getProduct().getImageUrl());
  }

  @NonNull
  public String getAdvertiserDomain() {
    return assets.getAdvertiserDomain();
  }

  @NonNull
  public String getAdvertiserDescription() {
    return assets.getAdvertiserDescription();
  }

  @NonNull
  public CriteoMedia getAdvertiserLogoMedia() {
    return CriteoMedia.create(assets.getAdvertiserLogoUrl());
  }

  /**
   * Create a new rendered native view.
   * <p>
   * The native view is directly usable. You can add it to your view hierarchy.
   * <p>
   * Note that parent is given so that inflated views are setup with the {@link
   * android.view.ViewGroup.LayoutParams} corresponding to their parent. See {@link
   * CriteoNativeRenderer} for more information.
   *
   * @param context android context
   * @param parent optional parent to get layout params from
   * @return new renderer native view
   */
  @NonNull
  public View createNativeRenderedView(@NonNull Context context, @Nullable ViewGroup parent) {
    View nativeView = renderer.createNativeView(context, parent);
    renderNativeView(nativeView);
    return nativeView;
  }

  /**
   * Render the given native view.
   * <p>
   * This view should come from {@link #createNativeRenderedView(Context, ViewGroup)} or from {@link
   * CriteoNativeLoader#createEmptyNativeView(Context, ViewGroup)}.
   * <p>
   * It is safe to call this method many times on the same view or on different views. This will
   * render many times the ad.
   * <p>
   * See {@link CriteoNativeRenderer} for more information.
   *
   * @param nativeView native you to render
   */
  public void renderNativeView(@NonNull View nativeView) {
    renderer.renderNativeView(rendererHelper, nativeView, this);

    watchForImpression(nativeView);
    setProductClickableView(nativeView);

    ImageView adChoiceView = adChoiceOverlay.getAdChoiceView(nativeView);
    if (adChoiceView != null) {
      setAdChoiceClickableView(adChoiceView);
      rendererHelper.setMediaInView(
          assets.getPrivacyOptOutImageUrl(),
          adChoiceView,
          /* placeholder */ null // No placeholder is expected for AdChoice
      );
    }
  }

  @Internal({MOPUB_ADAPTER, ADMOB_ADAPTER})
  void setRenderer(@NonNull CriteoNativeRenderer renderer) {
    this.renderer = renderer;
  }

  /**
   * Watch the given view for impression.
   * <p>
   * This method can be called many times on the same instance. But the impression is triggered only
   * once.
   * <p>
   * It is not necessary to clean any state before calling this method, even if the given view was
   * already tracked.
   *
   * @param nativeView view to start tracking the impression
   * @see VisibilityTracker
   */
  @VisibleForTesting
  void watchForImpression(@NonNull View nativeView) {
    visibilityTracker.watch(nativeView, impressionTask);
  }

  /**
   * Set the given views as a clickable region representing this ad product.
   * <p>
   * This method can be called many times on the same instance with different views. Click may be
   * triggered several times.
   * <p>
   * It is not necessary to clean any state before calling this method, even if the given view was
   * already watched.
   *
   * @param nativeView view to start watching for clicks
   */
  @VisibleForTesting
  void setProductClickableView(@NonNull View nativeView) {
    clickDetection.watch(nativeView, clickOnProductHandler);
  }

  /**
   * Set the given views as a clickable region representing this Ad opt out.
   *
   * @param adChoiceView view to start watching for clicks
   * @see #setProductClickableView(View)
   */
  @Internal(ADMOB_ADAPTER)
  void setAdChoiceClickableView(@NonNull View adChoiceView) {
    clickDetection.watch(adChoiceView, clickOnAdChoiceHandler);
  }

  /**
   * Return the AdChoice placeholder injected by {@link AdChoiceOverlay#addOverlay(View)}.
   *
   * @param overlappedView view to get the AdChoice from
   * @return AdChoice view
   * @see AdChoiceOverlay#getAdChoiceView(View)
   */
  @Nullable
  @Internal(ADMOB_ADAPTER)
  ImageView getAdChoiceView(@NonNull View overlappedView) {
    return adChoiceOverlay.getAdChoiceView(overlappedView);
  }

}
