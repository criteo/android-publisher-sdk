package com.criteo.publisher;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.AppLifecycleUtil;
import com.criteo.publisher.Util.DeviceUtil;

import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.network.PubSdkApi;
import java.util.ArrayList;
import java.util.List;

final class CriteoInternal extends Criteo {

  private static final String TAG = CriteoInternal.class.getSimpleName();

  private BidManager bidManager;
  private AppEvents appEvents;
  private AppLifecycleUtil appLifecycleUtil;
  private DeviceInfo deviceInfo;
  private Config config;

  CriteoInternal(
      Application application,
      List<AdUnit> adUnits,
      String criteoPublisherId,
      DependencyProvider dependencyProvider) {
    if (application == null) {
      throw new IllegalArgumentException("Application reference is required.");
    }

    if (TextUtils.isEmpty(criteoPublisherId)) {
      throw new IllegalArgumentException("Criteo Publisher Id is required.");
    }

    if (adUnits == null) {
      adUnits = new ArrayList<>();
    }

    Context context = application.getApplicationContext();
    DeviceUtil deviceUtil = dependencyProvider.provideDeviceUtil(context);
    deviceUtil.createSupportedScreenSizes(application);

    deviceInfo = dependencyProvider.provideDeviceInfo();
    config = dependencyProvider.provideConfig(context);

    Clock clock = dependencyProvider.provideClock();

    AndroidUtil androidUtil = dependencyProvider.provideAndroidUtil(context);

    PubSdkApi api = dependencyProvider.providePubSdkApi(context);

    bidManager = dependencyProvider.provideBidManager(
        context,
        criteoPublisherId,
        deviceInfo,
        config,
        deviceUtil,
        dependencyProvider.provideLoggingUtil(),
        clock,
        dependencyProvider.provideUserPrivacyUtil(context),
        dependencyProvider.provideAdUnitMapper(androidUtil, deviceUtil),
        api);

    this.appEvents = new AppEvents(
        context,
        deviceUtil,
        clock,
        api);

    this.appLifecycleUtil = new AppLifecycleUtil(application, appEvents, bidManager);

    List<AdUnit> prefetchAdUnits = adUnits;
    deviceInfo.initialize(context, new UserAgentCallback() {
      @Override
      public void done() {
        bidManager.prefetch(prefetchAdUnits);
      }
    });
  }

  @Override
  public void setBidsForAdUnit(Object object, AdUnit adUnit) {
    try {
      doSetBidsForAdUnit(object, adUnit);
    } catch (Throwable e) {
      Log.e(TAG, "Internal error while setting bids for adUnit.", e);
    }
  }

  private void doSetBidsForAdUnit(Object object, AdUnit adUnit) {
    if (bidManager == null) {
      return;
    }
    bidManager.enrichBid(object, adUnit);
  }

  /**
   * Method to start new CdbDownload Asynctask
   */
  @Override
  Slot getBidForAdUnit(AdUnit adUnit) {
    if (bidManager == null) {
      return null;
    }
    return bidManager.getBidForAdUnitAndPrefetch(adUnit);
  }

  @Override
  public BidResponse getBidResponse(AdUnit adUnit) {
    BidResponse response;

    try {
      response = doGetBidResponse(adUnit);
    } catch (Throwable e) {
      response = new BidResponse();
      Log.e(TAG, "Internal error while getting Bid Response.", e);
    }

    return response;
  }

  private BidResponse doGetBidResponse(AdUnit adUnit) {
    if (bidManager == null) {
      return null;
    }
    return bidManager.getBidForInhouseMediation(adUnit);
  }

  @Override
  TokenValue getTokenValue(BidToken bidToken, AdUnitType adUnitType) {
    if (bidManager == null) {
      return null;
    }
    return bidManager.getTokenValue(bidToken, adUnitType);
  }

  @Override
  DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  @Override
  Config getConfig() {
    return config;
  }
}
