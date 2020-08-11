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
import com.criteo.publisher.model.AdSize;

public class DeviceUtil {

  @NonNull
  private final Context context;

  public DeviceUtil(@NonNull Context context) {
    this.context = context;
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

  public AdSize getCurrentScreenSize() {
    DisplayMetrics metrics = getDisplayMetrics();
    int widthInDp = Math.round(metrics.widthPixels / metrics.density);
    int heightInDp = Math.round(metrics.heightPixels / metrics.density);
    return new AdSize(widthInDp, heightInDp);
  }

  private DisplayMetrics getDisplayMetrics() {
    return context.getResources().getDisplayMetrics();
  }

  public boolean isVersionSupported() {
    if (android.os.Build.VERSION.SDK_INT < 19) {
      Log.e(TAG, "Unsupported Android version");
      return false;
    }

    return true;
  }
}
