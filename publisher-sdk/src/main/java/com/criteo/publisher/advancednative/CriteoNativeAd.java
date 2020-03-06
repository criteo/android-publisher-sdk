package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
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

  public CriteoNativeAd(
      @NonNull NativeAssets assets,
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionTask impressionTask,
      @NonNull ClickDetection clickDetection,
      @NonNull NativeViewClickHandler clickOnProductHandler
  ) {
    this.assets = assets;
    this.visibilityTracker = visibilityTracker;
    this.impressionTask = impressionTask;
    this.clickDetection = clickDetection;
    this.clickOnProductHandler = clickOnProductHandler;
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

}
