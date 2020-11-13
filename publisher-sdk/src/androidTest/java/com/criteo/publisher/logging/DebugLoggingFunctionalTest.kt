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
import android.content.Context
import com.criteo.publisher.Bid
import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.SdkInitLogMessage
import com.criteo.publisher.TestAdUnits.BANNER_320_50
import com.criteo.publisher.TestAdUnits.BANNER_UNKNOWN
import com.criteo.publisher.headerbidding.AppBiddingLogMessage
import com.criteo.publisher.integration.Integration.CUSTOM_APP_BIDDING
import com.criteo.publisher.integration.Integration.GAM_APP_BIDDING
import com.criteo.publisher.integration.Integration.MOPUB_APP_BIDDING
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.InterstitialAdUnit
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.util.BuildConfigWrapper
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.mopub.mobileads.MoPubView
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
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

  @Inject
  private lateinit var context: Context

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

  @Test
  fun whenEnrichingAnyAppBidding_NoBid_LogFailure() {
    givenInitializedCriteo()

    Criteo.getInstance().loadBid(BANNER_UNKNOWN) {
      Criteo.getInstance().enrichAdObjectWithBid(PublisherAdRequest.Builder(), it)
      Criteo.getInstance().enrichAdObjectWithBid(MoPubView(context), it)
      Criteo.getInstance().enrichAdObjectWithBid(HashMap<String, String>(), it)
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger, times(3)).log(AppBiddingLogMessage.onTryingToEnrichAdObjectFromBid(null))
    verify(logger).log(AppBiddingLogMessage.onAdObjectEnrichedWithNoBid(GAM_APP_BIDDING))
    verify(logger).log(AppBiddingLogMessage.onAdObjectEnrichedWithNoBid(MOPUB_APP_BIDDING))
    verify(logger).log(AppBiddingLogMessage.onAdObjectEnrichedWithNoBid(CUSTOM_APP_BIDDING))
  }

  @Test
  fun whenEnrichingAdMob_BannerBid_LogSuccess() {
    lateinit var bid: Bid
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(BANNER_320_50) {
      Criteo.getInstance().enrichAdObjectWithBid(PublisherAdRequest.Builder(), it)
      bid = it!!
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(AppBiddingLogMessage.onTryingToEnrichAdObjectFromBid(bid))
    verify(logger).log(check {
      assertThat(it.message).contains(GAM_APP_BIDDING.toString()).contains("crt_cpm")
    })
  }

  @Test
  fun whenEnrichingMoPub_BannerBid_LogSuccess() {
    lateinit var bid: Bid
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(BANNER_320_50) {
      Criteo.getInstance().enrichAdObjectWithBid(MoPubView(context), it)
      bid = it!!
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(AppBiddingLogMessage.onTryingToEnrichAdObjectFromBid(bid))
    verify(logger).log(check {
      assertThat(it.message).contains(MOPUB_APP_BIDDING.toString()).contains("crt_cpm")
    })
  }

  @Test
  fun whenEnrichingCustom_BannerBid_LogSuccess() {
    lateinit var bid: Bid
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(BANNER_320_50) {
      Criteo.getInstance().enrichAdObjectWithBid(HashMap<String, String>(), it)
      bid = it!!
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(AppBiddingLogMessage.onTryingToEnrichAdObjectFromBid(bid))
    verify(logger).log(check {
      assertThat(it.message).contains(CUSTOM_APP_BIDDING.toString()).contains("crt_cpm")
    })
  }

  @Test
  fun whenEnrichingUnknown_LogError() {
    lateinit var bid: Bid
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(BANNER_320_50) {
      Criteo.getInstance().enrichAdObjectWithBid("unknown", it)
      bid = it!!
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(AppBiddingLogMessage.onTryingToEnrichAdObjectFromBid(bid))
    verify(logger).log(AppBiddingLogMessage.onUnknownAdObjectEnriched("unknown"))
  }

  @Test
  fun whenEnrichingNull_LogError() {
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(BANNER_320_50) {
      Criteo.getInstance().enrichAdObjectWithBid(null, it)
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(AppBiddingLogMessage.onUnknownAdObjectEnriched(null))
  }
}
