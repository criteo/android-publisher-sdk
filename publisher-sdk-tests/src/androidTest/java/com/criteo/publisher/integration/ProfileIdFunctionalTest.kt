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
import com.criteo.publisher.csm.MetricHelper
import com.criteo.publisher.csm.MetricSendingQueueConsumer
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.network.PubSdkApi
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class ProfileIdFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var metricSendingQueueConsumer: MetricSendingQueueConsumer

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

  @Test
  fun csm_GivenPrefetchWithSdkUsedForTheFirstTime_UseFallbackProfileId() {
    givenInitializedCriteo(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    triggerMetricRequest()

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.FALLBACK.profileId)
      }
    })
  }

  @Test
  fun csm_GivenPrefetchWithUsedSdk_UseLatestProfileId() {
    givenInitializedCriteo()
    Criteo.getInstance().getBidResponse(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    // Clean the metric yields above to avoid interference
    triggerMetricRequest()

    mockedDependenciesRule.resetAllDependencies()

    givenInitializedCriteo(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    triggerMetricRequest()

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.IN_HOUSE.profileId)
        assertThat(it.internalFeedbacks).hasSize(1)
      }
    })
  }

  @Test
  fun csm_GivenIntegrationSpecificBidConsumedWithSdkUsedForTheFirstTime_UseIntegrationProfileId() {
    givenInitializedCriteo()
    Criteo.getInstance().getBidResponse(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    triggerMetricRequest()

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.IN_HOUSE.profileId)
      }
    })
  }

  @Test
  fun csm_GivenIntegrationSpecificBidConsumedWithUsedSdkByAnotherIntegration_GroupMetricsByProfileId() {
    // Deactivate the CSM sending to show that SDK is handling sending different profile IDs
    doNothing().whenever(metricSendingQueueConsumer).sendMetricBatch()

    givenInitializedCriteo()
    Criteo.getInstance().getBidResponse(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().setBidsForAdUnit(mutableMapOf<Any, Any>(), BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    doCallRealMethod().whenever(metricSendingQueueConsumer).sendMetricBatch()
    triggerMetricRequest()

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.IN_HOUSE.profileId)
        assertThat(it.internalFeedbacks).hasSize(1)
      }
    })

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.CUSTOM_APP_BIDDING.profileId)
        assertThat(it.internalFeedbacks).hasSize(1)
      }
    })
  }

  private fun triggerMetricRequest() {
    // CSM are put in queue during SDK init but they are not sent, so we need to trigger it.
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()
    metricSendingQueueConsumer.sendMetricBatch()
    mockedDependenciesRule.waitForIdleState()
  }

}