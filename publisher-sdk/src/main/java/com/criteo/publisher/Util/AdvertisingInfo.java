package com.criteo.publisher.Util;

import android.content.Context;
import android.util.Log;

public class AdvertisingInfo {

  private static final String GET_ADVERTISING_ID = "getId";
  private static final String IS_LIMIT_AD_TRACKING_ENABLED = "isLimitAdTrackingEnabled";

  public String getAdvertisingId(Context context) {
    try {
      return (String) ReflectionUtil.callAdvertisingIdInfo(GET_ADVERTISING_ID, context);
    } catch (Exception e) {
      Log.e("AdvertisingInfo", "Error getting advertising id: " + e.getMessage());
    }
    return null;
  }

  public boolean isLimitAdTrackingEnabled(Context context) {
    try {
      Object isLimitAdTrackingEnabled = ReflectionUtil
          .callAdvertisingIdInfo(IS_LIMIT_AD_TRACKING_ENABLED, context);
      return (boolean) isLimitAdTrackingEnabled;
    } catch (Exception e) {
      Log.e("AdvertisingInfo", "Error checking if ad tracking is limited: " + e.getMessage());
    }
    return false;
  }
}
