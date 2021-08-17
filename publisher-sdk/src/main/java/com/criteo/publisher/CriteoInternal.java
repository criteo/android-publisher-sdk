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

import static com.criteo.publisher.ErrorLogMessage.onUncaughtErrorAtPublicApi;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.context.UserData;
import com.criteo.publisher.headerbidding.HeaderBidding;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import java.util.List;
import java.util.concurrent.Executor;

class CriteoInternal extends Criteo {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

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
  private final ConsumableBidLoader consumableBidLoader;

  @NonNull
  private final HeaderBidding headerBidding;

  @NonNull
  private final InterstitialActivityHelper interstitialActivityHelper;

  CriteoInternal(
      Application application,
      @NonNull List<AdUnit> adUnits,
      @Nullable Boolean usPrivacyOptout,
      @Nullable String mopubConsent,
      @NonNull DependencyProvider dependencyProvider
  ) {
    this.dependencyProvider = dependencyProvider;

    dependencyProvider.provideSession();

    deviceInfo = dependencyProvider.provideDeviceInfo();
    deviceInfo.initialize();

    dependencyProvider.provideAdvertisingInfo().prefetchAsync();

    config = dependencyProvider.provideConfig();

    bidManager = dependencyProvider.provideBidManager();
    consumableBidLoader = dependencyProvider.provideConsumableBidLoader();
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

    application.registerActivityLifecycleCallbacks(dependencyProvider.provideAppLifecycleUtil());

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
  public void enrichAdObjectWithBid(Object object, @Nullable Bid bid) {
    try {
      doSetBidsForAdUnit(object, bid);
    } catch (Throwable e) {
      logger.log(onUncaughtErrorAtPublicApi(e));
    }
  }

  private void doSetBidsForAdUnit(Object object, @Nullable Bid bid) {
    headerBidding.enrichBid(object, bid);
  }

  /**
   * Method to start new CdbDownload Asynctask [Standalone only]
   */
  @Override
  void getBidForAdUnit(@Nullable AdUnit adUnit, @NonNull ContextData contextData, @NonNull BidListener bidListener) {
    bidManager.getBidForAdUnit(adUnit, contextData, bidListener);
  }

  @Override
  public void loadBid(
      @NonNull AdUnit adUnit,
      @NonNull ContextData contextData,
      @NonNull BidResponseListener bidResponseListener
  ) {
    try {
      consumableBidLoader.loadBid(adUnit, contextData, bidResponseListener);
    } catch (Throwable e) {
      logger.log(onUncaughtErrorAtPublicApi(e));
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

  @Override
  public void setUserData(@NonNull UserData userData) {
    dependencyProvider.provideUserDataHolder().set(userData);
  }
}
