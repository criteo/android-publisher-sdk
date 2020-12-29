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

import android.content.Context
import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.CriteoInterstitial
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.TestAdUnits.BANNER_320_480
import com.criteo.publisher.TestAdUnits.INTERSTITIAL
import com.criteo.publisher.TestAdUnits.NATIVE
import com.criteo.publisher.advancednative.CriteoNativeLoader
import com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.csm.MetricHelper
import com.criteo.publisher.csm.MetricRepository
import com.criteo.publisher.csm.MetricSendingQueueConsumer
import com.criteo.publisher.csm.MetricSendingQueueProducer
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.privacy.ConsentData
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doCallRealMethod
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executor
import javax.inject.Inject

class ProfileIdFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var context: Context

  @SpyBean
  private lateinit var metricSendingQueueConsumer: MetricSendingQueueConsumer

  @SpyBean
  private lateinit var metricSendingQueueProducer: MetricSendingQueueProducer

  @SpyBean
  private lateinit var api: PubSdkApi

  @SpyBean
  private lateinit var threadPoolExecutor: Executor

  @SpyBean
  private lateinit var metricRepository: MetricRepository

  @SpyBean
  private lateinit var consentData: ConsentData

  @Test
  fun prefetch_GivenSdkUsedForTheFirstTime_UseFallbackProfileId() {
    givenInitializedCriteo(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    verifyCdbIsCalledWith(Integration.FALLBACK)
  }

  @Test
  fun prefetch_GivenUsedSdk_UseLatestProfileId() {
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo(BANNER_320_480)
    mockedDependenciesRule.waitForIdleState()

    verifyCdbIsCalledWith(Integration.IN_HOUSE)
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
    givenPreviousInHouseIntegrationWithResetDependencies()

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
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenConsentGiven()

    // Clean the metrics generated from the above InHouse bidding, to avoid interference
    triggerMetricRequest()
    clearInvocations(api)

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
    givenConsentGiven()

    bidStandaloneInterstitial()

    triggerMetricRequest()

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.STANDALONE.profileId)
      }
    })
  }

  @Test
  fun csm_GivenIntegrationSpecificBidConsumedWithUsedSdkByAnotherIntegration_GroupMetricsByProfileId() {
    // Deactivate the CSM sending to show that SDK is handling sending different profile IDs
    doNothing().whenever(metricSendingQueueConsumer).sendMetricBatch()

    givenInitializedCriteo()

    givenConsentGiven()

    bidStandaloneInterstitial()
    bidOtherAdServer()

    doCallRealMethod().whenever(metricSendingQueueConsumer).sendMetricBatch()
    triggerMetricRequest()

    verify(api).postCsm(check {
      with(MetricHelper) {
        assertThat(it.internalProfileId).isEqualTo(Integration.STANDALONE.profileId)
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

  @Test
  fun bidStandaloneBanner_GivenAnyPreviousIntegration_UseStandaloneProfileId() {
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()
    runOnMainThreadAndWait {
      CriteoBannerView(context, BANNER_320_480).loadAd(ContextData())
    }
    mockedDependenciesRule.waitForIdleState()

    verifyCdbIsCalledWith(Integration.STANDALONE)
  }

  @Test
  fun bidStandaloneInterstitial_GivenAnyPreviousIntegration_UseStandaloneProfileId() {
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()
    CriteoInterstitial(INTERSTITIAL).loadAd(ContextData())
    mockedDependenciesRule.waitForIdleState()

    verifyCdbIsCalledWith(Integration.STANDALONE)
  }

  @Test
  fun bidStandaloneNative_GivenAnyPreviousIntegration_UseStandaloneProfileId() {
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()
    CriteoNativeLoader(NATIVE, mock(), mock()).loadAd(ContextData())
    mockedDependenciesRule.waitForIdleState()

    verifyCdbIsCalledWith(Integration.STANDALONE)
  }

  @Test
  fun bidInHouseBanner_GivenAnyPreviousIntegration_UseInHouseProfileId() {
    fun loadBid() {
      Criteo.getInstance().loadBid(BANNER_320_480, ContextData()) {
        CriteoBannerView(context).loadAd(it)
      }

      mockedDependenciesRule.waitForIdleState()
    }

    givenPreviousStandaloneIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: InHouse integration is detected after bid request when loadAd method is invoked.
    loadBid()
    loadBid()


    verifyCdbIsCalledWith(Integration.IN_HOUSE)
  }

  @Test
  fun bidInHouseInterstitial_GivenAnyPreviousIntegration_UseInHouseProfileId() {
    givenPreviousStandaloneIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: InHouse integration is detected after bid request when loadAd method is invoked.
    bidInHouseInterstitial()
    bidInHouseInterstitial()

    verifyCdbIsCalledWith(Integration.IN_HOUSE)
  }

  @Test
  fun bidInHouseNative_GivenAnyPreviousIntegration_UseInHouseProfileId() {
    fun loadBid() {
      Criteo.getInstance().loadBid(NATIVE, ContextData()) {
        CriteoNativeLoader(mock(), mock()).loadAd(it)
      }
      mockedDependenciesRule.waitForIdleState()
    }

    givenPreviousStandaloneIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: InHouse integration is detected after bid request when loadAd method is invoked.
    loadBid()
    loadBid()

    verifyCdbIsCalledWith(Integration.IN_HOUSE)
  }

  @Test
  fun bidCustomAppBidding_GivenAnyPreviousIntegration_UseCustomAppBiddingProfileId() {
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: AppBidding integration is detected after bid request when enrich method is invoked.
    bidOtherAdServer()
    bidOtherAdServer()

    verifyCdbIsCalledWith(Integration.CUSTOM_APP_BIDDING)
  }

  @Test
  fun bidGamAppBidding_GivenAnyPreviousIntegration_UseGamAppBiddingProfileId() {
    fun loadBid() {
      Criteo.getInstance().loadBid(BANNER_320_480, ContextData()) {
        Criteo.getInstance().enrichAdObjectWithBid(PublisherAdRequest.Builder(), it)
      }
      mockedDependenciesRule.waitForIdleState()
    }

    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: AppBidding integration is detected after bid request when enrich method is invoked.
    loadBid()
    loadBid()

    verifyCdbIsCalledWith(Integration.GAM_APP_BIDDING)
  }

  @Test
  fun bidMoPubAppBiddingBanner_GivenAnyPreviousIntegration_UseMoPubAppBiddingProfileId() {
    val mopubView = callOnMainThreadAndWait { MoPubView(context) }

    fun loadBid() {
      Criteo.getInstance().loadBid(BANNER_320_480, ContextData()) {
        Criteo.getInstance().enrichAdObjectWithBid(mopubView, it)
      }
      mockedDependenciesRule.waitForIdleState()
    }
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: AppBidding integration is detected after bid request when enrich method is invoked.
    loadBid()
    loadBid()

    verifyCdbIsCalledWith(Integration.MOPUB_APP_BIDDING)
  }

  @Test
  fun bidMoPubAppBiddingInterstitial_GivenAnyPreviousIntegration_UseMoPubAppBiddingProfileId() {
    val moPubInterstitial = callOnMainThreadAndWait {
      MoPubInterstitial(mock(), "adUnit")
    }

    fun loadBid() {
      Criteo.getInstance().loadBid(BANNER_320_480, ContextData()) {
        Criteo.getInstance().enrichAdObjectWithBid(moPubInterstitial, it)
      }
      mockedDependenciesRule.waitForIdleState()
    }
    givenPreviousInHouseIntegrationWithResetDependencies()

    givenInitializedCriteo()

    // Need 2 bids: AppBidding integration is detected after bid request when enrich method is invoked.
    loadBid()
    loadBid()

    verifyCdbIsCalledWith(Integration.MOPUB_APP_BIDDING)
  }

  private fun givenPreviousInHouseIntegrationWithResetDependencies() {
    givenInitializedCriteo()
    bidInHouseInterstitial()
    mockedDependenciesRule.resetAllDependencies()
  }

  private fun givenPreviousStandaloneIntegrationWithResetDependencies() {
    givenInitializedCriteo()
    bidStandaloneInterstitial()
    mockedDependenciesRule.resetAllDependencies()
  }

  private fun triggerMetricRequest() {
    metricSendingQueueProducer.pushAllInQueue(metricRepository)
    mockedDependenciesRule.waitForIdleState()
    metricSendingQueueConsumer.sendMetricBatch()
    mockedDependenciesRule.waitForIdleState()
  }

  private fun verifyCdbIsCalledWith(integration: Integration) {
    verify(api, atLeastOnce()).loadCdb(check {
      assertThat(it.profileId).isEqualTo(integration.profileId)
    }, any())
  }

  private fun bidInHouseInterstitial() {
    Criteo.getInstance().loadBid(INTERSTITIAL, ContextData()) {
      CriteoInterstitial().loadAd(it)
    }
    mockedDependenciesRule.waitForIdleState()
  }

  private fun bidStandaloneInterstitial() {
    CriteoInterstitial(INTERSTITIAL).loadAd(ContextData())
    mockedDependenciesRule.waitForIdleState()
  }

  private fun bidOtherAdServer() {
    Criteo.getInstance().loadBid(BANNER_320_480, ContextData()) {
      Criteo.getInstance().enrichAdObjectWithBid(mutableMapOf<Any, Any>(), it)
    }
    mockedDependenciesRule.waitForIdleState()
  }

  private fun givenConsentGiven() {
    whenever(consentData.consentGiven).thenReturn(true)
  }
}
