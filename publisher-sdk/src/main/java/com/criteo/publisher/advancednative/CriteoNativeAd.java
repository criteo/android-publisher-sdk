package com.criteo.publisher.advancednative;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.criteo.publisher.annotation.Incubating;
import com.criteo.publisher.model.nativeads.NativeAssets;
import java.net.URL;

@Incubating(Incubating.NATIVE)
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
  private final CriteoNativeRenderer renderer;

  public CriteoNativeAd(
      @NonNull NativeAssets assets,
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionTask impressionTask,
      @NonNull ClickDetection clickDetection,
      @NonNull NativeViewClickHandler clickOnProductHandler,
      @NonNull NativeViewClickHandler clickOnAdChoiceHandler,
      @NonNull AdChoiceOverlay adChoiceOverlay,
      @NonNull CriteoNativeRenderer renderer
  ) {
    this.assets = assets;
    this.visibilityTracker = visibilityTracker;
    this.impressionTask = impressionTask;
    this.clickDetection = clickDetection;
    this.clickOnProductHandler = clickOnProductHandler;
    this.clickOnAdChoiceHandler = clickOnAdChoiceHandler;
    this.adChoiceOverlay = adChoiceOverlay;
    this.renderer = renderer;
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
  public URL getProductImageUrl() {
    return assets.getProduct().getImageUrl();
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
  public URL getAdvertiserLogoImageUrl() {
    return assets.getAdvertiserLogoUrl();
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
    View nativeView = addAdChoiceOverlay(renderer.createNativeView(context, parent));
    renderer.renderNativeView(nativeView, this);

    watchForImpression(nativeView);
    setProductClickableView(nativeView);

    ImageView adChoiceView = adChoiceOverlay.getAdChoiceView(nativeView);
    if (adChoiceView != null) {
      setAdChoiceClickableView(adChoiceView);
    }

    return nativeView;
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
  @VisibleForTesting
  void setAdChoiceClickableView(@NonNull View adChoiceView) {
    clickDetection.watch(adChoiceView, clickOnAdChoiceHandler);
  }

  /**
   * Add an overlay on the given view, with a placeholder for AdChoice icon.
   *
   * @param view view to wrap with AdChoice
   * @return view with the overlay
   * @see AdChoiceOverlay#addOverlay(View)
   */
  @NonNull
  @VisibleForTesting
  ViewGroup addAdChoiceOverlay(@NonNull View view) {
    return adChoiceOverlay.addOverlay(view);
  }

  /**
   * Return the AdChoice placeholder injected by {@link #addAdChoiceOverlay(View)}.
   *
   * @param overlappedView view to get the AdChoice from
   * @return AdChoice view
   * @see AdChoiceOverlay#getAdChoiceView(View)
   */
  @Nullable
  @VisibleForTesting
  ImageView getAdChoiceView(@NonNull View overlappedView) {
    return adChoiceOverlay.getAdChoiceView(overlappedView);
  }

}
