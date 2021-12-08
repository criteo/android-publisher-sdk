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
import android.util.Log
import com.criteo.publisher.BannerLogMessage
import com.criteo.publisher.Bid
import com.criteo.publisher.BiddingLogMessage
import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.CriteoInterstitial
import com.criteo.publisher.CriteoUtil
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.SdkInitLogMessage
import com.criteo.publisher.StubConstants
import com.criteo.publisher.TestAdUnits.BANNER_320_50
import com.criteo.publisher.TestAdUnits.BANNER_UNKNOWN
import com.criteo.publisher.TestAdUnits.INTERSTITIAL
import com.criteo.publisher.TestAdUnits.INTERSTITIAL_UNKNOWN
import com.criteo.publisher.TestAdUnits.NATIVE
import com.criteo.publisher.TestAdUnits.NATIVE_UNKNOWN
import com.criteo.publisher.advancednative.CriteoNativeLoader
import com.criteo.publisher.advancednative.NativeLogMessage
import com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait
import com.criteo.publisher.headerbidding.AppBiddingLogMessage
import com.criteo.publisher.integration.Integration.CUSTOM_APP_BIDDING
import com.criteo.publisher.integration.Integration.GAM_APP_BIDDING
import com.criteo.publisher.integration.Integration.MOPUB_APP_BIDDING
import com.criteo.publisher.interstitial.InterstitialLogMessage
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.InterstitialAdUnit
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.network.NetworkLogMessage
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.privacy.PrivacyLogMessage
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.mopub.mobileads.MoPubView
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.check
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class DebugLoggingFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

  @Inject
  private lateinit var application: Application

  @Inject
  private lateinit var context: Context

  @Inject
  private lateinit var jsonSerializer: JsonSerializer

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @SpyBean
  private lateinit var api: PubSdkApi

  @SpyBean
  private lateinit var consoleHandler: ConsoleHandler

  @SpyBean
  private lateinit var logger: Logger

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
      Criteo.getInstance().enrichAdObjectWithBid(AdManagerAdRequest.Builder(), it)
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
      Criteo.getInstance().enrichAdObjectWithBid(AdManagerAdRequest.Builder(), it)
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

  @Test
  fun whenLoadingBid_NoBid_LogFailure() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(BANNER_UNKNOWN) {
      // no op
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(BiddingLogMessage.onConsumableBidLoaded(BANNER_UNKNOWN, null))
  }

  @Test
  fun whenLoadingBid_ValidBid_LogSuccess() {
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    val bidRef = AtomicReference<Bid>()

    Criteo.getInstance().loadBid(BANNER_320_50, bidRef::set)
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(BiddingLogMessage.onConsumableBidLoaded(BANNER_320_50, bidRef.get()))
  }

  @Test
  fun whenLoadingBannerForStandalone_ValidBid_LogSuccess() {
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    val bannerView = callOnMainThreadAndWait { CriteoBannerView(context, BANNER_320_50) }
    bannerView.loadAd()
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(BannerLogMessage.onBannerViewInitialized(BANNER_320_50))
    verify(logger).log(BannerLogMessage.onBannerViewLoading(bannerView))
    verify(logger).log(BannerLogMessage.onBannerViewLoaded(bannerView))
  }

  @Test
  fun whenLoadingBannerForStandalone_NoBid_LogFailure() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    val bannerView = callOnMainThreadAndWait { CriteoBannerView(context, BANNER_UNKNOWN) }
    bannerView.loadAd()
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(BannerLogMessage.onBannerViewInitialized(BANNER_UNKNOWN))
    verify(logger).log(BannerLogMessage.onBannerViewLoading(bannerView))
    verify(logger).log(BannerLogMessage.onBannerViewFailedToLoad(bannerView))
  }

  @Test
  fun whenLoadingBannerForInHouse_ValidBid_LogSuccess() {
    lateinit var bid: Bid
    givenInitializedCriteo(BANNER_320_50)
    mockedDependenciesRule.waitForIdleState()

    val bannerView = callOnMainThreadAndWait { CriteoBannerView(context) }
    Criteo.getInstance().loadBid(BANNER_320_50) {
      bid = it!!
      bannerView.loadAd(it)
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(BannerLogMessage.onBannerViewInitialized(null))
    verify(logger).log(BannerLogMessage.onBannerViewLoading(bannerView, bid))
    verify(logger).log(BannerLogMessage.onBannerViewLoaded(bannerView))
  }

  @Test
  fun whenLoadingBannerForInHouse_NoBid_LogFailure() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    val bannerView = callOnMainThreadAndWait { CriteoBannerView(context) }
    Criteo.getInstance().loadBid(BANNER_UNKNOWN, bannerView::loadAd)
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(BannerLogMessage.onBannerViewInitialized(null))
    verify(logger).log(BannerLogMessage.onBannerViewLoading(bannerView, null))
    verify(logger).log(BannerLogMessage.onBannerViewFailedToLoad(bannerView))
  }

  @Test
  fun whenLoadingInterstitialForStandalone_ValidBid_LogSuccess() {
    givenInitializedCriteo(INTERSTITIAL)
    mockedDependenciesRule.waitForIdleState()

    val interstitial = CriteoInterstitial(INTERSTITIAL)
    interstitial.loadAd()
    mockedDependenciesRule.waitForIdleState()

    if (interstitial.isAdLoaded) {
      interstitial.show()
    }

    verify(logger).log(InterstitialLogMessage.onInterstitialInitialized(INTERSTITIAL))
    verify(logger).log(InterstitialLogMessage.onInterstitialLoading(interstitial))
    verify(logger).log(InterstitialLogMessage.onInterstitialLoaded(interstitial))
    verify(logger).log(InterstitialLogMessage.onCheckingIfInterstitialIsLoaded(interstitial, true))
    verify(logger).log(InterstitialLogMessage.onInterstitialShowing(interstitial))
  }

  @Test
  fun whenLoadingInterstitialForStandalone_NoBid_LogFailure() {
    givenInitializedCriteo(INTERSTITIAL_UNKNOWN)
    mockedDependenciesRule.waitForIdleState()

    val interstitial = CriteoInterstitial(INTERSTITIAL_UNKNOWN)
    interstitial.loadAd()
    mockedDependenciesRule.waitForIdleState()

    if (interstitial.isAdLoaded) {
      interstitial.show()
    }

    verify(logger).log(InterstitialLogMessage.onInterstitialInitialized(INTERSTITIAL_UNKNOWN))
    verify(logger).log(InterstitialLogMessage.onInterstitialLoading(interstitial))
    verify(logger).log(InterstitialLogMessage.onInterstitialFailedToLoad(interstitial))
    verify(logger).log(InterstitialLogMessage.onCheckingIfInterstitialIsLoaded(interstitial, false))
  }

  @Test
  fun whenLoadingInterstitialForInHouse_ValidBid_LogSuccess() {
    lateinit var bid: Bid
    givenInitializedCriteo(INTERSTITIAL)
    mockedDependenciesRule.waitForIdleState()

    val interstitial = CriteoInterstitial()
    Criteo.getInstance().loadBid(INTERSTITIAL) {
      interstitial.loadAd(it)
      bid = it!!
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(InterstitialLogMessage.onInterstitialInitialized(null))
    verify(logger).log(InterstitialLogMessage.onInterstitialLoading(interstitial, bid))
    verify(logger).log(InterstitialLogMessage.onInterstitialLoaded(interstitial))
  }

  @Test
  fun whenLoadingInterstitialForInHouse_NoBid_LogFailure() {
    givenInitializedCriteo(INTERSTITIAL_UNKNOWN)
    mockedDependenciesRule.waitForIdleState()

    val interstitial = CriteoInterstitial()
    Criteo.getInstance().loadBid(INTERSTITIAL_UNKNOWN, interstitial::loadAd)
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(InterstitialLogMessage.onInterstitialInitialized(null))
    verify(logger).log(InterstitialLogMessage.onInterstitialLoading(interstitial, null))
    verify(logger).log(InterstitialLogMessage.onInterstitialFailedToLoad(interstitial))
  }

  @Test
  fun whenLoadingNativeForStandalone_ValidBid_LogSuccess() {
    givenInitializedCriteo(NATIVE)
    mockedDependenciesRule.waitForIdleState()

    val nativeLoader = CriteoNativeLoader(NATIVE, mock(), mock())
    nativeLoader.loadAd()
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(NativeLogMessage.onNativeLoaderInitialized(NATIVE))
    verify(logger).log(NativeLogMessage.onNativeLoading(nativeLoader))
    verify(logger).log(NativeLogMessage.onNativeLoaded(nativeLoader))
  }

  @Test
  fun whenLoadingNativeForStandalone_NoBid_LogFailure() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    val nativeLoader = CriteoNativeLoader(NATIVE_UNKNOWN, mock(), mock())
    nativeLoader.loadAd()
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(NativeLogMessage.onNativeLoaderInitialized(NATIVE_UNKNOWN))
    verify(logger).log(NativeLogMessage.onNativeLoading(nativeLoader))
    verify(logger).log(NativeLogMessage.onNativeFailedToLoad(nativeLoader))
  }

  @Test
  fun whenLoadingNativeForInHouse_ValidBid_LogSuccess() {
    lateinit var bid: Bid
    givenInitializedCriteo(NATIVE)
    mockedDependenciesRule.waitForIdleState()

    val nativeLoader = CriteoNativeLoader(mock(), mock())
    Criteo.getInstance().loadBid(NATIVE) {
      nativeLoader.loadAd(it)
      bid = it!!
    }
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(NativeLogMessage.onNativeLoaderInitialized(null))
    verify(logger).log(NativeLogMessage.onNativeLoading(nativeLoader, bid))
    verify(logger).log(NativeLogMessage.onNativeLoaded(nativeLoader))
  }

  @Test
  fun whenLoadingNativeForInHouse_NoBid_LogFailure() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    val nativeLoader = CriteoNativeLoader(mock(), mock())
    Criteo.getInstance().loadBid(NATIVE_UNKNOWN, nativeLoader::loadAd)
    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(NativeLogMessage.onNativeLoaderInitialized(null))
    verify(logger).log(NativeLogMessage.onNativeLoading(nativeLoader, null))
    verify(logger).log(NativeLogMessage.onNativeFailedToLoad(nativeLoader))
  }

  @Test
  fun whenDoingACdbRequest_LogRequestAndResponse() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().loadBid(NATIVE) {
      // no op
    }
    mockedDependenciesRule.waitForIdleState()

    argumentCaptor<CdbRequest> {
      verify(api).loadCdb(capture(), any())

      verify(logger).log(NetworkLogMessage.onCdbCallStarted(jsonSerializer.writeIntoString(lastValue)))
    }

    verify(logger).log(check {
      assertThat(it.message?.withoutWhitespace()).contains(StubConstants.STUB_NATIVE_JSON.withoutWhitespace())
    })
  }

  @Test
  fun whenSettingConsentDuringInit_LogThatConsentIsSet() {
    Criteo.Builder(application, "any")
        .mopubConsent("myMoPubConsent")
        .usPrivacyOptOut(true)
        .init()

    mockedDependenciesRule.waitForIdleState()

    verify(logger).log(PrivacyLogMessage.onMoPubConsentSet("myMoPubConsent"))
    verify(logger).log(PrivacyLogMessage.onUsPrivacyOptOutSet(true))
  }

  @Test
  fun whenSettingConsentAfterInit_LogThatConsentIsSet() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    Criteo.getInstance().setMopubConsent("myMoPubConsent")
    Criteo.getInstance().setUsPrivacyOptOut(true)

    verify(logger).log(PrivacyLogMessage.onMoPubConsentSet("myMoPubConsent"))
    verify(logger).log(PrivacyLogMessage.onUsPrivacyOptOutSet(true))
  }

  @Test
  fun whenDebugLogsIsEnabled_LogsInConsoleHandler() {
    assumeInfoLogsAreNotExpectedByDefault()

    givenInitializedCriteo(true)
    mockedDependenciesRule.waitForIdleState()

    // [SdkLogMessage.onSdkInitialized, IntegrationLogMessage.onNoDeclaredIntegration] are expected
    verify(consoleHandler, atLeastOnce()).println(eq(Log.INFO), any(), any())
  }

  @Test
  fun whenDebugLogsIsNotEnabled_LogsNothingInConsoleHandler() {
    assumeInfoLogsAreNotExpectedByDefault()

    givenInitializedCriteo(false)
    mockedDependenciesRule.waitForIdleState()

    verify(consoleHandler, never()).println(eq(Log.INFO), any(), any())
  }

  private fun assumeInfoLogsAreNotExpectedByDefault() {
    whenever(buildConfigWrapper.defaultMinLogLevel).doReturn(Log.WARN)

    consoleHandler.log("any", LogMessage(Log.INFO, "any"))

    verify(consoleHandler, never()).println(any(), any(), any())
    clearInvocations(consoleHandler)
  }

  private fun String.withoutWhitespace() = replace("\\s".toRegex(), "")

  private fun givenInitializedCriteo(isDebugLogsEnabled: Boolean = true) {
    CriteoUtil.getCriteoBuilder()
        .debugLogsEnabled(isDebugLogsEnabled)
        .init()
  }

}
