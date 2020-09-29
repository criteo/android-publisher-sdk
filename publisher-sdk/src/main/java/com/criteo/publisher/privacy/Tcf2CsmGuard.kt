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
    const val IAB_VENDOR_LEGITIMATE_INTERESTS = "IABTCF_VendorLegitimateInterests"
    const val IAB_PUBLISHER_RESTRICTION_FOR_PURPOSE_1 = "IABTCF_PublisherRestrictions1"
    const val IAB_PURPOSE_CONSENTS = "IABTCF_PurposeConsents"

    /**
     * The Vendor ID of Criteo is 91 (which is 1-based). So consents for Criteo is at index 90 (which is 0-based).
     */
    const val CRITEO_VENDOR_INDEX = 90
  }

  /**
   * Indicate if user didn't give its consent for CSM when applicable.
   *
   * - IABTCF_PurposeConsents : User consent to purpose
   * - IABTCF_PublisherRestrictions{ID}: publisher restrictions on purpose {ID} (only ID=1 is required for storing
   * technical data)
   *     - if RestrictionType = 0: Purpose Flatly Not Allowed by Publisher => **No CSM**
   *     - if RestrictionType = 1: Consent is required > Ignore restriction > fallback on other consent checks
   *     - if RestrictionType = 2: Legitimate interest is required > **No CSM**
   * - IABTCF_VendorConsents AND IABTCF_VendorLegitimateInterests:
   *     - VendorConsent: Check Bitfield OR Range whether Criteo (91) == 1
   *     - VendorLegitimateInterest: Check Bitfield OR Range whether Criteo (91) == 1
   *     - If Criteo (91) is not set to 1 in either VendorConsent or VendorLegitimateInterest: **No CSM**
   */
  fun isCsmDisallowed(): Boolean {
    val purposeConsentNotGiven = isPurpose1ConsentGiven() == false

    val publisherRestriction = getPublisherRestrictionTypeForPurpose1()
    val hasPublisherRestriction = publisherRestriction in setOf(
        PublisherRestrictionType.NOT_ALLOWED,
        PublisherRestrictionType.REQUIRE_LEGITIMATE_INTEREST
    )

    if (purposeConsentNotGiven || hasPublisherRestriction) {
      return true
    }

    return isVendorConsentGiven() == false && isVendorLegitimateInterestGiven() == false
  }

  @VisibleForTesting
  fun isVendorConsentGiven(): Boolean? {
    return readCriteoConsentInBinaryString(IAB_VENDOR_CONSENTS)
  }

  @VisibleForTesting
  fun isVendorLegitimateInterestGiven(): Boolean? {
    return readCriteoConsentInBinaryString(IAB_VENDOR_LEGITIMATE_INTERESTS)
  }

  @VisibleForTesting
  fun getPublisherRestrictionTypeForPurpose1(): PublisherRestrictionType? {
    val criteoChar = readCriteoCharInString(IAB_PUBLISHER_RESTRICTION_FOR_PURPOSE_1) ?: return null

    return when (criteoChar) {
      '0' -> PublisherRestrictionType.NOT_ALLOWED
      '1' -> PublisherRestrictionType.REQUIRE_CONSENT
      '2' -> PublisherRestrictionType.REQUIRE_LEGITIMATE_INTEREST
      else -> null
    }
  }

  @VisibleForTesting
  fun isPurpose1ConsentGiven(): Boolean? {
    // Purpose 1 (1-based) is at index 0 (0-based)
    return readCharInString(IAB_PURPOSE_CONSENTS, 0).toBoolean()
  }

  private fun readCriteoConsentInBinaryString(key: String): Boolean? = readCriteoCharInString(key).toBoolean()

  private fun readCriteoCharInString(key: String): Char? = readCharInString(key, CRITEO_VENDOR_INDEX)

  private fun readCharInString(key: String, index: Int): Char? {
    val string = safeSharedPreferences.getNonNullString(key, "")
    return string.elementAtOrNull(index)
  }

  private fun Char?.toBoolean(): Boolean? {
    return when (this) {
      '0' -> false
      '1' -> true
      else -> null
    }
  }

  enum class PublisherRestrictionType {
    NOT_ALLOWED,
    REQUIRE_CONSENT,
    REQUIRE_LEGITIMATE_INTEREST
  }
}
