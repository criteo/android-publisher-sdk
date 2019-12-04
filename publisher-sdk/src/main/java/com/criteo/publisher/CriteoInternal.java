package com.criteo.publisher;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.AppLifecycleUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserAgentCallback;

import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitHelper;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.util.ArrayList;
import java.util.List;

final class CriteoInternal extends Criteo {

  private static final String TAG = CriteoInternal.class.getSimpleName();

  private BidManager bidManager;
  private AppEvents appEvents;
  private AppLifecycleUtil appLifecycleUtil;
  private DeviceInfo deviceInfo;
  private Config config;

  CriteoInternal(Application application, List<AdUnit> adUnits, String criteoPublisherId) {
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
    DeviceUtil deviceUtil = DependencyProvider.getInstance().provideDeviceUtil(context);
    createSupportedScreenSizes(application, deviceUtil);

    AndroidUtil androidUtil = DependencyProvider.getInstance().provideAndroidUtil(context);


    List<CacheAdUnit> cacheAdUnits = AdUnitHelper
        .convertAdUnits(adUnits, androidUtil.getOrientation(), deviceUtil);

    List<CacheAdUnit> validatedCacheAdUnits = AdUnitHelper.filterInvalidCacheAdUnits(cacheAdUnits);

    AdvertisingInfo advertisingInfo = DependencyProvider.getInstance().provideAdvertisingInfo();
    this.deviceInfo = new DeviceInfo();
    config = DependencyProvider.getInstance().provideConfig(context);

    Clock clock = DependencyProvider.getInstance().provideClock();

    this.bidManager = new BidManager(
        context,
        criteoPublisherId,
        validatedCacheAdUnits,
        deviceInfo,
        config,
        androidUtil,
        deviceUtil,
        DependencyProvider.getInstance().provideLoggingUtil(),
        advertisingInfo,
        clock
    );

    this.appEvents = new AppEvents(context, deviceUtil, clock);
    this.appLifecycleUtil = new AppLifecycleUtil(application, appEvents, bidManager);

    deviceInfo.initialize(context, new UserAgentCallback() {
      @Override
      public void done() {
        bidManager.prefetch();
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

  private void createSupportedScreenSizes(Application application, DeviceUtil deviceUtil) {
    try {
      DisplayMetrics metrics = new DisplayMetrics();
      ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
          .getMetrics(metrics);
      deviceUtil.setScreenSize(Math.round(metrics.widthPixels / metrics.density),
          Math.round(metrics.heightPixels / metrics.density));
    } catch (Exception e) {
      // FIXME(ma.chentir) message might be misleading as this could not be the only exception cause
      throw new Error("Screen parameters can not be empty or null");
    }
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
