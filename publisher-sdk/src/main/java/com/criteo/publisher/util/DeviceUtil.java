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

package com.criteo.publisher.util;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.model.AdSize;

public class DeviceUtil {

  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";

  private static AdSize sizePortrait = new AdSize(0, 0);
  private static AdSize sizeLandscape = new AdSize(0, 0);

  @NonNull
  private final Context context;

  @NonNull
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
    if (advertisingInfo.isLimitAdTrackingEnabled()) {
      return DEVICE_ID_LIMITED;
    }
    return advertisingInfo.getAdvertisingId();
  }

  public int isLimitAdTrackingEnabled() {
    // FIXME This entire method seems dumb. It's just a mapping from bool to 0,1
    return advertisingInfo.isLimitAdTrackingEnabled() ? 1 : 0;
  }

  public boolean isVersionSupported() {
    if (android.os.Build.VERSION.SDK_INT < 19) {
      Log.e(TAG, "Unsupported Android version");
      return false;
    }

    return true;
  }
}
