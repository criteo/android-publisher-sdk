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

package com.criteo.publisher.integration

import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.TestAdUnits.BANNER_320_480
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.network.PubSdkApi
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class ProfileIdFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var api: PubSdkApi

  @Test
  fun prefetch_GivenSdkUsedForTheFirstTime_UseFallbackProfileId() {
    givenInitializedCriteo(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    verify(api).loadCdb(check {
      assertThat(it.profileId).isEqualTo(Integration.FALLBACK.profileId)
    }, any())
  }

  @Test
  fun prefetch_GivenUsedSdk_UseLatestProfileId() {
    givenInitializedCriteo()
    Criteo.getInstance().getBidResponse(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    mockedDependenciesRule.resetAllDependencies()

    givenInitializedCriteo(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    verify(api).loadCdb(check {
      assertThat(it.profileId).isEqualTo(Integration.IN_HOUSE.profileId)
    }, any())
  }

  @Test
  fun remoteConfig_GivenSdkUsedForTheFirstTime_UseFallbackProfileId() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    verify(api).loadConfig(check {
      assertThat(it.profileId).isEqualTo(Integration.FALLBACK.profileId)
    })
  }

  @Test
  fun remoteConfig_GivenUsedSdk_UseLastProfileId() {
    givenInitializedCriteo()
    Criteo.getInstance().getBidResponse(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    mockedDependenciesRule.resetAllDependencies()

    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    verify(api).loadConfig(check {
      assertThat(it.profileId).isEqualTo(Integration.IN_HOUSE.profileId)
    })
  }

}