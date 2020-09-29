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

import com.criteo.publisher.privacy.Tcf2CsmGuard.PublisherRestrictionType
import com.criteo.publisher.privacy.Tcf2CsmGuard.PublisherRestrictionType.NOT_ALLOWED
import com.criteo.publisher.privacy.Tcf2CsmGuard.PublisherRestrictionType.REQUIRE_CONSENT
import com.criteo.publisher.privacy.Tcf2CsmGuard.PublisherRestrictionType.REQUIRE_LEGITIMATE_INTEREST
import com.criteo.publisher.util.SafeSharedPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class Tcf2CsmGuardTest {

  @Mock
  private lateinit var sharedPref: SafeSharedPreferences

  private lateinit var csmGuard: Tcf2CsmGuard

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    csmGuard = Tcf2CsmGuard(sharedPref)
  }

  @Test
  fun isCsmDisallowed_GivenEmptySharedPref_ReturnFalse() {
    // Given
    whenever(sharedPref.getString(any(), any())).doAnswer {
      it.getArgument(1)
    }

    // When
    val csmDisallowed = csmGuard.isCsmDisallowed()

    // Then
    assertThat(csmDisallowed).isFalse()
  }

  @Test
  fun isCsmDisallowed_GivenTcf2_PublisherRestrictionNotProvidedOrRequireConsent_PurposeContentGivenOrNotProvided() {
    // Neither vendor consent nor legitimate interest => No CSM
    isCsmDisallowed_PublisherRestrictionNotProvidedOrRequireConsent_PurposeContentGivenOrNotProvided(
        VendorConsents.NOT_GIVEN,
        VendorLegitimateInterest.NOT_GIVEN,
        true
    )

    // Either vendor consent or legitimate interest => CSM is ok
    for (vendorConsent in VendorConsents.ALL) {
      for (vendorLegitimateInterest in VendorLegitimateInterest.ALL) {
        if (vendorConsent == VendorConsents.NOT_GIVEN &&
            vendorLegitimateInterest == VendorLegitimateInterest.NOT_GIVEN) {
          continue
        }

        isCsmDisallowed_PublisherRestrictionNotProvidedOrRequireConsent_PurposeContentGivenOrNotProvided(
            vendorConsent,
            vendorLegitimateInterest,
            false
        )
      }
    }
  }

  private fun isCsmDisallowed_PublisherRestrictionNotProvidedOrRequireConsent_PurposeContentGivenOrNotProvided(
      vendorConsent: String,
      vendorLegitimateInterest: String,
      expected: Boolean
  ) {
    for (publisherRestriction in listOf(
        PublisherRestrictionTypeForPurpose1.NOT_PROVIDED,
        PublisherRestrictionTypeForPurpose1.REQUIRE_CONSENT
    )) {
      for (purposeConsent in listOf(PurposeContent.NOT_PROVIDED, PurposeContent.GIVEN)) {
        isCsmDisallowed_GivenTcf2(
            vendorConsent,
            vendorLegitimateInterest,
            publisherRestriction,
            purposeConsent,
            expected
        )
      }
    }
  }

  @Test
  fun isCsmDisallowed_GivenTcf2_PublisherRestrictionForPurpose1NotAllowedOrRequireLegitimateInterest() {
    for (vendorConsent in VendorConsents.ALL) {
      for (vendorLegitimateInterest in VendorLegitimateInterest.ALL) {
        isCsmDisallowed_GivenTcf2(
            vendorConsent,
            vendorLegitimateInterest,
            PublisherRestrictionTypeForPurpose1.NOT_ALLOWED,
            PurposeContent.GIVEN,
            true
        )

        isCsmDisallowed_GivenTcf2(
            vendorConsent,
            vendorLegitimateInterest,
            PublisherRestrictionTypeForPurpose1.REQUIRE_LEGITIMATE_INTEREST,
            PurposeContent.GIVEN,
            true
        )
      }
    }
  }

  @Test
  fun isCsmDisallowed_GivenTcf2_PurposeConsentNotGiven() {
    for (vendorConsent in VendorConsents.ALL) {
      for (vendorLegitimateInterest in VendorLegitimateInterest.ALL) {
        for (publisherRestriction in PublisherRestrictionTypeForPurpose1.ALL) {
          isCsmDisallowed_GivenTcf2(
              vendorConsent,
              vendorLegitimateInterest,
              publisherRestriction,
              PurposeContent.NOT_GIVEN,
              true
          )
        }
      }
    }
  }

  private fun isCsmDisallowed_GivenTcf2(
      vendorConsent: String,
      vendorLegitimateInterest: String,
      publisherRestrictionTypeForPurpose1: String,
      purposeConsent: String,
      expected: Boolean
  ) {
    // Given
    sharedPref.stub {
      on { getString("IABTCF_VendorConsents", "") } doReturn vendorConsent
      on { getString("IABTCF_VendorLegitimateInterests", "") } doReturn vendorLegitimateInterest
      on { getString("IABTCF_PublisherRestrictions1", "") } doReturn publisherRestrictionTypeForPurpose1
      on { getString("IABTCF_PurposeConsents", "") } doReturn purposeConsent
    }

    // When
    val csmDisallowed = csmGuard.isCsmDisallowed()

    // Then
    assertThat(csmDisallowed).describedAs(
        """
          vendorConsent=%s
          vendorLegitimateInterest=%s
          publisherRestrictionTypeForPurpose1=%s
          purposeConsent=%s
        """.trimIndent(),
        vendorConsent,
        vendorLegitimateInterest,
        publisherRestrictionTypeForPurpose1,
        purposeConsent
    ).isEqualTo(expected)
  }

  @Test
  fun testVendorConsentGiven() {
    testVendorConsentGiven("", null)
    testVendorConsentGiven("malformed", null)
    testVendorConsentGiven(String.format("%090dX", 0), null)
    testVendorConsentGiven(String.format("%091d", 0), false)
    testVendorConsentGiven(String.format("%091d", 1), true)
  }

  private fun testVendorConsentGiven(value: String, expected: Boolean?) {
    // Given
    sharedPref.stub {
      on { getString("IABTCF_VendorConsents", "") } doReturn value
    }

    // When
    val isVendorConsentGiven = csmGuard.isVendorConsentGiven()

    // Then
    assertThat(isVendorConsentGiven).isEqualTo(expected)
  }

  @Test
  fun testVendorLegitimateInterest() {
    testVendorLegitimateInterest("", null)
    testVendorLegitimateInterest("malformed", null)
    testVendorLegitimateInterest(String.format("%090dX", 0), null)
    testVendorLegitimateInterest(String.format("%091d", 0), false)
    testVendorLegitimateInterest(String.format("%091d", 1), true)
  }

  private fun testVendorLegitimateInterest(value: String, expected: Boolean?) {
    // Given
    sharedPref.stub {
      on { getString("IABTCF_VendorLegitimateInterests", "") } doReturn value
    }

    // When
    val isVendorLegitimateInterestGiven = csmGuard.isVendorLegitimateInterestGiven()

    // Then
    assertThat(isVendorLegitimateInterestGiven).isEqualTo(expected)
  }

  @Test
  fun testPublisherRestrictionTypeForPurpose1() {
    testPublisherRestrictionTypeForPurpose1("", null)
    testPublisherRestrictionTypeForPurpose1("malformed", null)
    testPublisherRestrictionTypeForPurpose1(String.format("%090dX", 0), null)
    testPublisherRestrictionTypeForPurpose1(String.format("%091d", 0), NOT_ALLOWED)
    testPublisherRestrictionTypeForPurpose1(String.format("%091d", 1), REQUIRE_CONSENT)
    testPublisherRestrictionTypeForPurpose1(String.format("%091d", 2), REQUIRE_LEGITIMATE_INTEREST)
  }

  private fun testPublisherRestrictionTypeForPurpose1(value: String, expected: PublisherRestrictionType?) {
    // Given
    sharedPref.stub {
      on { getString("IABTCF_PublisherRestrictions1", "") } doReturn value
    }

    // When
    val publisherRestrictionTypeForPurpose1 = csmGuard.getPublisherRestrictionTypeForPurpose1()

    // Then
    assertThat(publisherRestrictionTypeForPurpose1).isEqualTo(expected)
  }

  @Test
  fun testPurpose1Consent() {
    testPurpose1Consent("", null)
    testPurpose1Consent("malformed", null)
    testPurpose1Consent(String.format("X%09d", 0), null)
    testPurpose1Consent(String.format("0%09d", 0), false)
    testPurpose1Consent(String.format("1%09d", 0), true)
  }

  private fun testPurpose1Consent(value: String, expected: Boolean?) {
    // Given
    sharedPref.stub {
      on { getString("IABTCF_PurposeConsents", "") } doReturn value
    }

    // When
    val isPurpose1ConsentGiven = csmGuard.isPurpose1ConsentGiven()

    // Then
    assertThat(isPurpose1ConsentGiven).isEqualTo(expected)
  }

  private object VendorConsents {
    const val NOT_PROVIDED = ""
    val NOT_GIVEN = String.format("%091d", 0)
    val GIVEN = String.format("%091d", 1)

    val ALL = listOf(NOT_PROVIDED, NOT_GIVEN, GIVEN)
  }

  private object VendorLegitimateInterest {
    const val NOT_PROVIDED = ""
    val NOT_GIVEN = String.format("%091d", 0)
    val GIVEN = String.format("%091d", 1)

    val ALL = listOf(NOT_PROVIDED, NOT_GIVEN, GIVEN)
  }

  private object PublisherRestrictionTypeForPurpose1 {
    const val NOT_PROVIDED = ""
    val NOT_ALLOWED = String.format("%091d", 0)
    val REQUIRE_CONSENT = String.format("%091d", 1)
    val REQUIRE_LEGITIMATE_INTEREST = String.format("%091d", 2)

    val ALL = listOf(NOT_PROVIDED, NOT_ALLOWED, REQUIRE_CONSENT, REQUIRE_LEGITIMATE_INTEREST)
  }

  private object PurposeContent {
    const val NOT_PROVIDED = ""
    val NOT_GIVEN = String.format("0%09d", 0)
    val GIVEN = String.format("1%09d", 0)
  }
}
