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
package com.criteo.publisher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.model.AdSize
import kotlin.math.min

@OpenForTesting
class DeviceUtil(private val context: Context) {

  /**
   * Indicate if the device is a tablet or not.
   *
   * The definition of a tablet is based on its
   * [smallest width](https://developer.android.com/training/multiscreen/screensizes.html#TaskUseSWQuali):
   * if width is above or equal to 600dp, then it is a tablet.
   *
   * The corollary is that, if this is not a tablet, then we consider this as a mobile.
   *
   * @return `true` if this device is a tablet
   */
  @Suppress("MagicNumber")
  fun isTablet(): Boolean {
    val metrics = displayMetrics
    val smallestWidthInPixel = min(metrics.widthPixels, metrics.heightPixels)
    val thresholdInPixel = 600f * metrics.density
    return smallestWidthInPixel >= thresholdInPixel
  }

  fun getCurrentScreenSize(): AdSize {
    val metrics = displayMetrics
    val widthInDp = pxToDp(metrics.widthPixels)
    val heightInDp = pxToDp(metrics.heightPixels)
    return AdSize(widthInDp, heightInDp)
  }

  /**
   *
   * @return device screenSize including status and navigation bar
   */
  fun getRealSceeenSize(): AdSize {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val widthPx: Int
    val heightPx: Int
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val windowMetrics = windowManager.maximumWindowMetrics
      widthPx = windowMetrics.bounds.width()
      heightPx = windowMetrics.bounds.height()
    } else {
      val point = Point()
      windowManager.defaultDisplay.getRealSize(point)
      widthPx = point.x
      heightPx = point.y
    }
    return AdSize(pxToDp(widthPx), pxToDp(heightPx))
  }

  fun canSendSms(): Boolean {
    val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:123456"))
    return canHandleIntent(smsIntent)
  }

  fun canInitiateCall(): Boolean {
    val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:123456"))
    return canHandleIntent(callIntent)
  }

  // Currently minimum supported version is 19 and minSdk is set to 19
  // return true since all versions starting from 19 are supported
  // Use this mechanism to deprecate SDK version before raising minSdk version
  @Suppress("FunctionOnlyReturningConstant")
  fun isVersionSupported(): Boolean {
    return true
  }

  fun pxToDp(pxValue: Int): Int {
    return Math.round(pxValue / displayMetrics.density)
  }

  private val displayMetrics: DisplayMetrics
    get() = context.resources.displayMetrics

  private fun canHandleIntent(intent: Intent): Boolean {
    val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.packageManager.queryIntentActivities(
          intent,
          PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
      )
    } else {
      context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }
    return activities.isNotEmpty()
  }
}
