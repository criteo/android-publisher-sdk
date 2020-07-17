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
import com.criteo.publisher.mock.MockedDependenciesRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class AdvertisingInfoTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var context: Context

  private lateinit var advertisingInfo: AdvertisingInfo

  @Before
  fun setUp() {
    advertisingInfo = AdvertisingInfo()
  }

  @Test
  fun getAdvertisingId_GivenPlayServiceAdsIdentifierInClasspath_ReturnNonNull() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val advertisingId = advertisingInfo.getAdvertisingId(context)

    assertThat(advertisingId).isNotNull()
  }

  @Test
  fun isLimitAdTrackingEnabled_GivenPlayServiceAdsIdentifierInClasspath_ReturnFalse() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val isLimitAdTrackingEnabled = advertisingInfo.isLimitAdTrackingEnabled(context)

    assertThat(isLimitAdTrackingEnabled).isFalse()
  }

}