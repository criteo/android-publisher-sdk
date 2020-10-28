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

import android.os.Build
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.filterNotNullValues

@OpenForTesting
internal class ContextProvider {

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

  internal fun fetchUserContext(): Map<String, Any> {
    return mapOf(
        DeviceMake to fetchDeviceMake(),
        DeviceModel to fetchDeviceModel()
    ).filterNotNullValues()
  }

  private companion object {
    const val DeviceMake = "device.make"
    const val DeviceModel = "device.model"
  }
}
