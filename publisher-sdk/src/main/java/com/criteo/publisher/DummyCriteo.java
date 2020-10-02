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

package com.criteo.publisher;

import static com.criteo.publisher.util.CompletableFuture.completedFuture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import java.util.concurrent.Future;

public class DummyCriteo extends Criteo {

  @Override
  public void setBidsForAdUnit(Object object, @NonNull AdUnit adUnit) {
    // Nothing
  }

  @Override
  void getBidForAdUnit(@Nullable AdUnit adUnit, @NonNull BidListener bidListener) {
    bidListener.onNoBid();
  }

  @Override
  public void loadBid(
      @NonNull AdUnit adUnit,
      @NonNull BidResponseListener bidResponseListener
  ) {
    bidResponseListener.onResponse(null);
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
  public CriteoBannerEventController createBannerController(@NonNull CriteoBannerView bannerView) {
    return new CriteoBannerEventController(
        bannerView,
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
    public void openActivity(
        @NonNull String webViewContent,
        @NonNull InterstitialListenerNotifier listenerNotifier
    ) {
    }
  }
}
