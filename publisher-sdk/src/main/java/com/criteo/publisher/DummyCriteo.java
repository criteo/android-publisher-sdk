package com.criteo.publisher;

import static com.criteo.publisher.util.CompletableFuture.completedFuture;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.util.AdUnitType;
import java.util.concurrent.Future;

public class DummyCriteo extends Criteo {

  @Override
  public void setBidsForAdUnit(Object object, AdUnit adUnit) {
    // Nothing
  }

  @Nullable
  @Override
  Slot getBidForAdUnit(@Nullable AdUnit adUnit) {
    return null;
  }

  @Override
  public BidResponse getBidResponse(AdUnit adUnit) {
    return new BidResponse();
  }

  @Nullable
  @Override
  DisplayUrlTokenValue getTokenValue(@Nullable BidToken bidToken, @NonNull AdUnitType adUnitType) {
    return null;
  }

  @NonNull
  @Override
  DeviceInfo getDeviceInfo() {
    return new DummyDeviceInfo();
  }

  @NonNull
  @Override
  Config getConfig() {
    return new Config();
  }

  @NonNull
  @Override
  InterstitialActivityHelper getInterstitialActivityHelper() {
    return new DummyInterstitialActivityHelper();
  }

  @NonNull
  @Override
  public CriteoBannerEventController createBannerController(CriteoBannerView bannerView) {
    return new CriteoBannerEventController(bannerView,
        this,
        DependencyProvider.getInstance().provideTopActivityFinder(),
        DependencyProvider.getInstance().provideRunOnUiThreadExecutor()
    );
  }

  @Override
  public void setUsPrivacyOptOut(boolean usPrivacyOptOut) {
    // do nothing
  }

  @Override
  public void setMopubConsent(String mopubConsent) {
    // do nothing
  }

  private static class DummyDeviceInfo extends DeviceInfo {

    private DummyDeviceInfo() {
      super(null, new RunOnUiThreadExecutor());
    }

    @NonNull
    @Override
    public Future<String> getUserAgent() {
      return completedFuture("");
    }

    @Override
    public void initialize() {
    }

  }

  private static class DummyInterstitialActivityHelper extends InterstitialActivityHelper {

    DummyInterstitialActivityHelper() {
      super(null, null);
    }

    @Override
    public boolean isAvailable() {
      return false;
    }

    @Override
    public void openActivity(@NonNull String webViewContent,
        @Nullable CriteoInterstitialAdListener listener) {
    }
  }
}
