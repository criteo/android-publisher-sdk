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

package com.criteo.publisher.context

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.core.os.ConfigurationCompat
import com.criteo.publisher.Session
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.AndroidUtil
import com.criteo.publisher.util.filterNotNullValues
import java.util.Locale

@OpenForTesting
@Suppress("TooManyFunctions")
internal class ContextProvider(
    private val context: Context,
    private val connectionTypeFetcher: ConnectionTypeFetcher,
    private val androidUtil: AndroidUtil,
    private val session: Session
) {

  /**
   * OpenRTB field: `device.make`
   *
   * ## Definition
   * Device make (e.g., "Apple")
   */
  internal fun fetchDeviceMake(): String? = Build.MANUFACTURER.takeIf { it != Build.UNKNOWN }

  /**
   * OpenRTB field: `device.model`
   *
   * ## Definition
   * Device model (e.g., “iPhone10,1” when the specific device model is known, “iPhone” otherwise). The value obtained
   * from the device O/S should be used when available.
   */
  internal fun fetchDeviceModel(): String? = Build.MODEL.takeIf { it != Build.UNKNOWN }

  /**
   * OpenRTB field: `device.contype`
   *
   * ## Definition
   * Network connection type. Refer to [ConnectionTypeFetcher.ConnectionType].
   */
  internal fun fetchDeviceConnectionType(): Int? {
    return connectionTypeFetcher.fetchConnectionType()?.openRtbValue
  }

  /**
   * OpenRTB field: `user.geo.country`
   *
   * ## Definition
   * ### Geo object
   * This object encapsulates various methods for specifying a geographic location. [...]. When subordinate to a User
   * object, it indicates the location of the user's home base (i.e., not necessarily their current location).
   *
   * ### Country property
   * Country code using ISO-3166-1-alpha-2.
   * *Note that alpha-3 codes may be encountered and vendors are encouraged to be tolerant of them.*
   */
  internal fun fetchUserCountry(): String? {
    return fetchLocales()
        .mapNotNull { it.country.takeIf { it.isNotBlank() } }
        .firstOrNull()
  }

  /**
   * Custom field: `data.inputLanguage`
   *
   * ## Definition
   * A string array containing the languages setup on the user's device keyboard. Country codes (ISO-3166-1-alpha-2) are
   * passed in the string array, where "en", "he" = English and Hebrew languages are enabled on the user's device
   * keyboard
   */
  internal fun fetchUserLanguages(): List<String>? {
    return fetchLocales()
        .mapNotNull { it.language.takeIf { it.isNotBlank() } }
        .distinct()
        .takeIf { it.isNotEmpty() }
  }

  private fun fetchLocales(): List<Locale> {
    val locales = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
    return Array(locales.size(), locales::get).toList()
  }

  /**
   * OpenRTB field: `device.w`
   *
   * ## Definition
   * Physical width of the screen in pixels.
   */
  internal fun fetchDeviceWidth(): Int? = fetchDevicePhysicalSize()?.x

  /**
   * OpenRTB field: `device.h`
   *
   * ## Definition
   * Physical height of the screen in pixels.
   */
  internal fun fetchDeviceHeight(): Int? = fetchDevicePhysicalSize()?.y

  private fun fetchDevicePhysicalSize(): Point? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return null
    }

    val point = Point()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getRealSize(point)
    return point
  }

  /**
   * Custom field: `data.orientation`
   *
   * ## Definition
   * Screen orientation ("Portrait" or "Landscape")
   */
  internal fun fetchDeviceOrientation(): String? {
    return when (androidUtil.orientation) {
      Configuration.ORIENTATION_PORTRAIT -> "Portrait"
      Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
      else -> null
    }
  }

  /**
   * Custom field: `data.sessionDuration`
   *
   * ## Definition
   * The total duration of time a user has spent so far in a specific app session expressed in seconds. For example, a
   * user has been playing Word Game for 45 seconds
   *
   * ## Note
   * This duration is approximate: it is the duration since the initialization of the SDK.
   */
  internal fun fetchSessionDuration(): Long? = session.getDuration()

  fun fetchUserContext(): Map<String, Any> {
    return mapOf(
        DeviceMake to fetchDeviceMake(),
        DeviceModel to fetchDeviceModel(),
        DeviceConnectionType to fetchDeviceConnectionType(),
        DeviceWidth to fetchDeviceWidth(),
        DeviceHeight to fetchDeviceHeight(),
        DeviceOrientation to fetchDeviceOrientation(),
        UserCountry to fetchUserCountry(),
        UserLanguages to fetchUserLanguages(),
        SessionDuration to fetchSessionDuration()
    ).filterNotNullValues()
  }

  private companion object {
    const val DeviceMake = "device.make"
    const val DeviceModel = "device.model"
    const val DeviceConnectionType = "device.contype"
    const val DeviceWidth = "device.w"
    const val DeviceHeight = "device.h"
    const val DeviceOrientation = "data.orientation"
    const val UserCountry = "user.geo.country"
    const val UserLanguages = "data.inputLanguage"
    const val SessionDuration = "data.sessionDuration"
  }
}
