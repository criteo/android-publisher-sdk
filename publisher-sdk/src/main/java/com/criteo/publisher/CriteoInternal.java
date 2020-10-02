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

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.headerbidding.HeaderBidding;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AppLifecycleUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

final class CriteoInternal extends Criteo {

  private static final String TAG = CriteoInternal.class.getSimpleName();

  @NonNull
  private final DependencyProvider dependencyProvider;

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final Config config;

  @NonNull
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final InHouse inHouse;

  @NonNull
  private final HeaderBidding headerBidding;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  CriteoInternal(
      Application application,
      List<AdUnit> adUnits,
      @Nullable Boolean usPrivacyOptout,
      @Nullable String mopubConsent,
      @NonNull DependencyProvider dependencyProvider
  ) {

    if (adUnits == null) {
      adUnits = new ArrayList<>();
    }

    this.dependencyProvider = dependencyProvider;

    deviceInfo = dependencyProvider.provideDeviceInfo();
    deviceInfo.initialize();

    config = dependencyProvider.provideConfig();

    bidManager = dependencyProvider.provideBidManager();
    inHouse = dependencyProvider.provideInHouse();
    headerBidding = dependencyProvider.provideHeaderBidding();

    interstitialActivityHelper = dependencyProvider.provideInterstitialActivityHelper();

    userPrivacyUtil = dependencyProvider.provideUserPrivacyUtil();
    if (usPrivacyOptout != null) {
      userPrivacyUtil.storeUsPrivacyOptout(usPrivacyOptout);
    }

    // this null check ensures that instantiating Criteo object with null mopub consent value,
    // doesn't erase the previously stored consent value
    if (mopubConsent != null) {
      userPrivacyUtil.storeMopubConsent(mopubConsent);
    }

    AppEvents appEvents = dependencyProvider.provideAppEvents();
    AppLifecycleUtil lifecycleCallback = new AppLifecycleUtil(appEvents, bidManager);
    application.registerActivityLifecycleCallbacks(lifecycleCallback);

    dependencyProvider.provideTopActivityFinder().registerActivityLifecycleFor(application);

    BidLifecycleListener bidLifecycleListener = dependencyProvider.provideBidLifecycleListener();
    bidLifecycleListener.onSdkInitialized();

    prefetchAdUnits(dependencyProvider.provideRunOnUiThreadExecutor(), adUnits);
  }

  private void prefetchAdUnits(Executor executor, List<AdUnit> adUnits) {
    executor.execute(new SafeRunnable() {
      @Override
      public void runSafely() {
        bidManager.prefetch(adUnits);
      }
    });
  }

  @Override
  public void setBidsForAdUnit(Object object, @NonNull AdUnit adUnit) {
    try {
      doSetBidsForAdUnit(object, adUnit);
    } catch (Throwable e) {
      Log.e(TAG, "Internal error while setting bids for adUnit.", e);
    }
  }

  private void doSetBidsForAdUnit(Object object, AdUnit adUnit) {
    headerBidding.enrichBid(object, adUnit);
  }

  /**
   * Method to start new CdbDownload Asynctask
   * [Standalone only]
   */
  @Override
  void getBidForAdUnit(AdUnit adUnit, @NonNull BidListener bidListener) {
    bidManager.getBidForAdUnit(adUnit, bidListener);
  }

  @Override
  public void loadBidResponse(
      @NonNull AdUnit adUnit,
      @NonNull BidResponseListener bidResponseListener
  ) {
    try {
      bidResponseListener.onResponse(inHouse.getBidResponse(adUnit));
    } catch (Throwable e) {
      Log.e(TAG, "Internal error while loading bid response.", e);
      bidResponseListener.onResponse(null);
    }
  }

  @NonNull
  @Override
  DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  @NonNull
  @Override
  Config getConfig() {
    return config;
  }

  @NonNull
  @Override
  InterstitialActivityHelper getInterstitialActivityHelper() {
    return interstitialActivityHelper;
  }

  @NonNull
  @Override
  public CriteoBannerEventController createBannerController(@NonNull CriteoBannerView bannerView) {
    return new CriteoBannerEventController(
        bannerView,
        this,
        dependencyProvider.provideTopActivityFinder(),
        dependencyProvider.provideRunOnUiThreadExecutor()
    );
  }

  @Override
  public void setUsPrivacyOptOut(boolean usPrivacyOptOut) {
    userPrivacyUtil.storeUsPrivacyOptout(usPrivacyOptOut);
  }

  @Override
  public void setMopubConsent(@Nullable String mopubConsent) {
    userPrivacyUtil.storeMopubConsent(mopubConsent);
  }
}
