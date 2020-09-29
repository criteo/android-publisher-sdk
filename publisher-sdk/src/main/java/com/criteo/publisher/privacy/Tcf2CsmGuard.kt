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

package com.criteo.publisher.privacy

import androidx.annotation.VisibleForTesting
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.SafeSharedPreferences
import com.criteo.publisher.util.getNonNullString

@OpenForTesting
internal class Tcf2CsmGuard(private val safeSharedPreferences: SafeSharedPreferences) {

  private companion object {
    const val IAB_VENDOR_CONSENTS = "IABTCF_VendorConsents"

    /**
     * The Vendor ID of Criteo is 91 (which is 1-based). So consents for Criteo is at index 90 (which is 0-based).
     */
    const val CRITEO_VENDOR_INDEX = 90
  }

  fun isCsmDisallowed(): Boolean {
    return isVendorConsentGiven() == false
  }

  @VisibleForTesting
  fun isVendorConsentGiven(): Boolean? {
    val vendorConsents = safeSharedPreferences.getNonNullString(IAB_VENDOR_CONSENTS, "")
    if (vendorConsents.length < CRITEO_VENDOR_INDEX) {
      return null
    }

    return when (vendorConsents[CRITEO_VENDOR_INDEX]) {
      '0' -> false
      '1' -> true
      else -> null
    }
  }
}
