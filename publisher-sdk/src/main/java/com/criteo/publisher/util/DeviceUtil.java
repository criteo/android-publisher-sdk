package com.criteo.publisher.util;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import com.criteo.publisher.model.AdSize;

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

  /**
   * Indicate if the device is a tablet or not.
   * <p>
   * The definition of a tablet is based on its <a href="https://developer.android.com/training/multiscreen/screensizes.html#TaskUseSWQuali">smallest
   * width</a>: if width is above or equal to 600dp, then it is a tablet.
   * <p>
   * The corollary is that, if this is not a tablet, then we consider this as a mobile.
   *
   * @return <code>true</code> if this device is a tablet
   */
  public boolean isTablet() {
    DisplayMetrics metrics = getDisplayMetrics();
    int smallestWidthInPixel = Math.min(metrics.widthPixels, metrics.heightPixels);
    float thresholdInPixel = 600.f * metrics.density;
    return smallestWidthInPixel >= thresholdInPixel;
  }

  public void createSupportedScreenSizes() {
    try {
      DisplayMetrics metrics = getDisplayMetrics();
      setScreenSize(Math.round(metrics.widthPixels / metrics.density),
          Math.round(metrics.heightPixels / metrics.density));
    } catch (Exception e) {
      // FIXME(ma.chentir) message might be misleading as this could not be the only exception cause
      throw new Error("Screen parameters can not be empty or null", e);
    }
  }

  private DisplayMetrics getDisplayMetrics() {
    return context.getResources().getDisplayMetrics();
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
