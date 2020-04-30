package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.nativeads.NativeAssets;
import java.net.URL;

import com.criteo.publisher.annotation.Incubating;

@Incubating(Incubating.NATIVE)
public class CriteoNativeAd {

  @NonNull
  private final NativeAssets assets;

  public CriteoNativeAd(@NonNull NativeAssets assets) {
    this.assets = assets;
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

}
