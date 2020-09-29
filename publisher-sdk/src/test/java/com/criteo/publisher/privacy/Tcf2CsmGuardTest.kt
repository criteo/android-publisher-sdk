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
  fun isCsmDisallowed_GivenTcf2() {
    // Neither vendor consent nor legitimate interest => No CSM
    isCsmDisallowed_GivenTcf2(VendorConsents.NOT_GIVEN, VendorLegitimateInterest.NOT_GIVEN, true)

    // Either vendor consent or legitimate interest => CSM is ok
    isCsmDisallowed_GivenTcf2(VendorConsents.NOT_PROVIDED, VendorLegitimateInterest.NOT_PROVIDED, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.NOT_GIVEN, VendorLegitimateInterest.NOT_PROVIDED, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.GIVEN, VendorLegitimateInterest.NOT_PROVIDED, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.NOT_PROVIDED, VendorLegitimateInterest.NOT_GIVEN, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.GIVEN, VendorLegitimateInterest.NOT_GIVEN, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.NOT_PROVIDED, VendorLegitimateInterest.GIVEN, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.NOT_GIVEN, VendorLegitimateInterest.GIVEN, false)
    isCsmDisallowed_GivenTcf2(VendorConsents.GIVEN, VendorLegitimateInterest.GIVEN, false)
  }

  private fun isCsmDisallowed_GivenTcf2(
      vendorConsent: String,
      vendorLegitimateInterest: String,
      expected: Boolean
  ) {
    // Given
    sharedPref.stub {
      on { getString("IABTCF_VendorConsents", "") } doReturn vendorConsent
      on { getString("IABTCF_VendorLegitimateInterests", "") } doReturn vendorLegitimateInterest
    }

    // When
    val csmDisallowed = csmGuard.isCsmDisallowed()

    // Then
    assertThat(csmDisallowed).describedAs(
        "vendorConsent=%s vendorLegitimateInterest=%s",
        vendorConsent,
        vendorLegitimateInterest
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

  private object VendorConsents {
    const val NOT_PROVIDED = ""
    val NOT_GIVEN = String.format("%091d", 0)
    val GIVEN = String.format("%091d", 1)
  }

  private object VendorLegitimateInterest {
    const val NOT_PROVIDED = ""
    val NOT_GIVEN = String.format("%091d", 0)
    val GIVEN = String.format("%091d", 1)
  }
}
