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

  public CriteoNativeAd(
      @NonNull NativeAssets assets,
      @NonNull VisibilityTracker visibilityTracker,
      @NonNull ImpressionTask impressionTask
  ) {
    this.assets = assets;
    this.visibilityTracker = visibilityTracker;
    this.impressionTask = impressionTask;
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

}
