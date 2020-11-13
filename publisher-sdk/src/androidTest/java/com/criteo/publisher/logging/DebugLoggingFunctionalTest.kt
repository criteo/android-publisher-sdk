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

package com.criteo.publisher.logging

import android.app.Application
import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.SdkInitLogMessage
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.InterstitialAdUnit
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class DebugLoggingFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule().withMockedLogger()

  @Inject
  private lateinit var application: Application

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  private lateinit var logger: Logger

  @Before
  fun setUp() {
    logger = mockedDependenciesRule.mockedLogger!!
  }

  @Test
  fun whenCriteoInitIsCalled_LogCpIdAndAdUnits() {
    whenever(buildConfigWrapper.sdkVersion).doReturn("1.2.3")
    val adUnits = listOf(
        BannerAdUnit("bannerAdUnit", AdSize(42, 1337)),
        InterstitialAdUnit("interstitialAdUnit"),
        NativeAdUnit("nativeAdUnit")
    )

    Criteo.Builder(application, "B-123456")
        .adUnits(adUnits)
        .init()

    verify(logger).log(SdkInitLogMessage.onSdkInitialized("B-123456", adUnits, "1.2.3"))
  }

  @Test
  fun whenCriteoInitMoreThanOnce_LogWarning() {
    givenInitializedCriteo()

    Criteo.Builder(application, "any").init()

    verify(logger).log(SdkInitLogMessage.onSdkInitializedMoreThanOnce())
  }
}
