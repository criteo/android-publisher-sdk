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

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.view.WindowMetrics;
import androidx.annotation.NonNull;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.AdSize;

public class DeviceUtil {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final Context context;

  public DeviceUtil(@NonNull Context context) {
    this.context = context;
  }

  /**
   * Indicate if the device is a tablet or not.
   * <p>
   * The definition of a tablet is based on its <a
   * href="https://developer.android.com/training/multiscreen/screensizes.html#TaskUseSWQuali">smallest
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
    int widthInDp = pxToDp(metrics.widthPixels);
    int heightInDp = pxToDp(metrics.heightPixels);

    return new AdSize(widthInDp, heightInDp);
  }

  /**
   *
   * @return device screenSize including status and navigation bar
   */
  public AdSize getRealScreenSize() {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    int widthPx;
    int heightPx;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      WindowMetrics windowMetrics = windowManager.getMaximumWindowMetrics();

      widthPx = windowMetrics.getBounds().width();
      heightPx = windowMetrics.getBounds().height();
    } else {
      Point point = new Point();
      windowManager.getDefaultDisplay().getRealSize(point);
      widthPx = point.x;
      heightPx = point.y;
    }

    return new AdSize(pxToDp(widthPx), pxToDp(heightPx));
  }

  private DisplayMetrics getDisplayMetrics() {
    return context.getResources().getDisplayMetrics();
  }

  private int pxToDp(int pxValue) {
    return Math.round(pxValue / getDisplayMetrics().density);
  }

  public boolean isVersionSupported() {
    // Currently minimum supported version is 19 and minSdk is set to 19
    // return true since all versions starting from 19 are supported
    // Use this mechanism to deprecate SDK version before raising minSdk version
    return true;
  }
}
