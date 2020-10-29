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

import android.content.res.Resources
import android.os.Build
import androidx.core.os.ConfigurationCompat
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.filterNotNullValues

@OpenForTesting
internal class ContextProvider(
    private val connectionTypeFetcher: ConnectionTypeFetcher
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
    val locales = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
    return Array(locales.size(), locales::get).toList()
        .mapNotNull { it.country.takeIf { it.isNotBlank() } }
        .firstOrNull()
  }

  internal fun fetchUserContext(): Map<String, Any> {
    return mapOf(
        DeviceMake to fetchDeviceMake(),
        DeviceModel to fetchDeviceModel(),
        DeviceConnectionType to fetchDeviceConnectionType(),
        UserCountry to fetchUserCountry()
    ).filterNotNullValues()
  }

  private companion object {
    const val DeviceMake = "device.make"
    const val DeviceModel = "device.model"
    const val DeviceConnectionType = "device.contype"
    const val UserCountry = "user.geo.country"
  }
}
