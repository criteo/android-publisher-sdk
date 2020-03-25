package com.criteo.publisher.Util;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.criteo.publisher.model.AdSize;
import java.util.Locale;

public class DeviceUtil {

  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

  private static AdSize sizePortrait = new AdSize(0, 0);
  private static AdSize sizeLandscape = new AdSize(0, 0);

  private final Context context;
  private final AdvertisingInfo advertisingInfo;

  public DeviceUtil(@NonNull Context context, @NonNull AdvertisingInfo advertisingInfo) {
    this.context = context;
    this.advertisingInfo = advertisingInfo;
  }

  public void createSupportedScreenSizes(Application application) {
    try {
      DisplayMetrics metrics = new DisplayMetrics();
      ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
          .getMetrics(metrics);
      setScreenSize(Math.round(metrics.widthPixels / metrics.density),
          Math.round(metrics.heightPixels / metrics.density));
    } catch (Exception e) {
      // FIXME(ma.chentir) message might be misleading as this could not be the only exception cause
      throw new Error("Screen parameters can not be empty or null", e);
    }
  }

  public void setScreenSize(int screenWidth, int screenHeight) {
    sizePortrait = new AdSize(screenWidth, screenHeight);
    sizeLandscape = new AdSize(screenHeight, screenWidth);
  }

  public AdSize getSizePortrait() {
    return sizePortrait;
  }

  public AdSize getSizeLandscape() {
    return sizeLandscape;
  }
  
  @Nullable
  public String getAdvertisingId() {
    try {
      if (advertisingInfo.isLimitAdTrackingEnabled(context)) {
        return DEVICE_ID_LIMITED;
      }
      return advertisingInfo.getAdvertisingId(context);
    } catch (Exception e) {
      // FIXME This seems like a dead code, because AdvertisingInfo already catch exceptions
      Log.e("DeviceUtil", "Error trying to get Advertising id: " + e.getMessage());
    }
    return null;
  }

  public int isLimitAdTrackingEnabled() {
    // FIXME This entire method seems dumb. It's just a mapping from bool to 0,1
    try {
      return advertisingInfo.isLimitAdTrackingEnabled(context) ? 1 : 0;
    } catch (Exception e) {
      // FIXME This seems like a dead code, because AdvertisingInfo already catch exceptions
      Log.e("DeviceUtil", "Error trying to check limited ad tracking: " + e.getMessage());
    }
    return 0;
  }

  public boolean isVersionSupported() {
    if (android.os.Build.VERSION.SDK_INT < 19) {
      Log.e(TAG, "Unsupported Android version");
      return false;
    }

    return true;
  }
}
