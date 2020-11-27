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

import com.criteo.publisher.logging.Logger
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.AdvertisingInfo.MissingPlayServicesAdsIdentifierException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class AdvertisingInfoNoIdentifierTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

  @SpyBean
  private lateinit var logger: Logger

  @Inject
  private lateinit var advertisingInfo: AdvertisingInfo

  @Before
  fun setUp() {
    assertThatCode {
      Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
    }.withFailMessage(
        """
The tests in this file validate that AdvertisingInfo feature is only degraded, but do not throw, if the
dependency is not provided at runtime.
This assertion check this test is ran without PlayServices Ads Identifier provided.
On IntelliJ, this assertion may appear wrong maybe because it takes some shortcut when creating test
class path.
To run those test locally and properly, you should use Gradle. Either via gradle command line or
via IntelliJ delegating test run to Gradle.
"""
    ).isInstanceOf(ClassNotFoundException::class.java)
  }

  @Test
  fun getAdvertisingId_GivenPlayServiceAdsIdentifierNotInClasspath_ReturnNull() {
    val advertisingId = advertisingInfo.advertisingId

    assertThat(advertisingId).isNull()
    verify(logger, times(2)).debug(any(), any<MissingPlayServicesAdsIdentifierException>())
  }

  @Test
  fun isLimitAdTrackingEnabled_GivenPlayServiceAdsIdentifierNotInClasspath_ReturnFalse() {
    val isLimitAdTrackingEnabled = advertisingInfo.isLimitAdTrackingEnabled

    assertThat(isLimitAdTrackingEnabled).isFalse()
    verify(logger).debug(any(), any<MissingPlayServicesAdsIdentifierException>())
  }
}
