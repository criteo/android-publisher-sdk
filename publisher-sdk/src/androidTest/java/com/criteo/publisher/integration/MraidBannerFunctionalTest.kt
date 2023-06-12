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

import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.test.filters.FlakyTest
import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.CriteoUtil
import com.criteo.publisher.MraidData
import com.criteo.publisher.R
import com.criteo.publisher.TestAdUnits
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.adview.Redirection
import com.criteo.publisher.callMraidObjectBlocking
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.getJavascriptResultBlocking
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdUnit
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.Config
import com.criteo.publisher.test.activity.DummyActivity
import com.criteo.publisher.view.WebViewLookup
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class MraidBannerFunctionalTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val activityRule = ActivityTestRule(
      DummyActivity::class.java
  )

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

  @SpyBean
  private lateinit var redirection: Redirection

  @SpyBean
  private lateinit var config: Config

  private lateinit var bannerView: CriteoBannerView

  private val validBannerAdUnit = TestAdUnits.BANNER_320_50

  private val webViewLookup = WebViewLookup()
  private val mraidData = MraidData()

  private lateinit var onReady: CountDownLatch
  private lateinit var onExpanded: CountDownLatch
  private lateinit var onHidden: CountDownLatch
  private lateinit var onDefault: CountDownLatch

  @Before
  fun setUp() {
    whenever(config.isMraidEnabled).thenReturn(true)

    onReady = CountDownLatch(1)
    onExpanded = CountDownLatch(1)
    onHidden = CountDownLatch(1)
    onDefault = CountDownLatch(1)

    givenInitializedSdk(validBannerAdUnit)
    bannerView = whenLoadingABanner(validBannerAdUnit)!!
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI and concurrency")
  fun whenExpand_ShouldMoveWebViewToDialogAndUpdateState() {
    waitForReady()

    expand()

    onExpanded.await()
    mockedDependenciesRule.waitForIdleState()

    assertThat(getCurrentState()).isEqualTo(MraidState.EXPANDED)
    assertThat(getWebView().parent).isNotNull
    assertThat(getWebView().parent).isNotEqualTo(bannerView)
    assertThat(bannerView.childCount).isEqualTo(1)
    assertThat(bannerView.getChildAt(0).id).isEqualTo(R.id.adWebViewPlaceholder)
    assertThat((getWebView().parent as ViewGroup).id).isEqualTo(R.id.adWebViewDialogContainer)
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI and concurrency")
  fun whenCloseInDefaultState_ShouldClearAd() {
    waitForReady()

    close()
    onHidden.await()
    mockedDependenciesRule.waitForIdleState()

    val content = webViewLookup.lookForHtmlContent(bannerView).get()

    assertThat(content).isEqualTo(mraidData.emptyPageHtml)
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI and concurrency")
  fun whenExpandAndThenClose_ShouldMoveBackToOriginalContainer() {
    waitForReady()

    val originalLayoutParams = getWebView().layoutParams
    expand()
    onExpanded.await()

    close()
    onDefault.await()
    mockedDependenciesRule.waitForIdleState()

    assertThat(getCurrentState()).isEqualTo(MraidState.DEFAULT)
    assertThat(getWebView().parent).isEqualTo(bannerView)
    assertThat(getWebView().layoutParams).isEqualTo(originalLayoutParams)
  }

  @Test
  @FlakyTest(detail = "Flakiness comes from UI and concurrency")
  fun whenOpen_ShouldDelegateToRedirection() {
    waitForReady()

    open()
    mockedDependenciesRule.waitForIdleState()

    verify(redirection).redirect(eq("https://www.criteo.com"), eq(activityRule.activity.componentName), any())
  }

  private fun givenInitializedSdk(vararg preloadedAdUnits: AdUnit) {
    CriteoUtil.givenInitializedCriteo(*preloadedAdUnits)
    waitForBids()
  }

  private fun waitForBids() {
    mockedDependenciesRule.waitForIdleState()
  }

  private fun whenLoadingABanner(bannerAdUnit: BannerAdUnit): CriteoBannerView? {
    val bannerView = ThreadingUtil.callOnMainThreadAndWait {
      val banner = CriteoBannerView(
          activityRule.activity,
          bannerAdUnit
      )
      banner.adWebView.addJavascriptInterface(this, "mraidTesterBridge")
      activityRule.activity.setContentView(banner)
      banner
    }

    val sync = CriteoSync(bannerView)
    loadAdAndWait(bannerView)
    sync.waitForDisplay()

    bannerView.adWebView.getJavascriptResultBlocking(mraidData.getTestJavascript())

    return bannerView
  }

  private fun loadAdAndWait(bannerView: CriteoBannerView) {
    bannerView.loadAd(ContextData())
    waitForBids()
  }

  private fun expand() {
    getWebView().callMraidObjectBlocking("expand()")
  }

  private fun close() {
    getWebView().callMraidObjectBlocking("close()")
  }

  private fun open() {
    getWebView().callMraidObjectBlocking("open(\"https://www.criteo.com\")")
  }

  private fun getCurrentState() = getWebView().getJavascriptResultBlocking("window.mraid.getState()")
      .toMraidState()

  private fun getWebView(): WebView {
    return bannerView.adWebView
  }

  private fun waitForReady() {
    onReady.await()
  }

  @JavascriptInterface
  fun onReady() {
    onReady.countDown()
  }

  @JavascriptInterface
  fun onStateChange(newState: String) {
    when (newState.toMraidState()) {
      MraidState.LOADING -> Unit
      MraidState.DEFAULT -> onDefault.countDown()
      MraidState.EXPANDED -> onExpanded.countDown()
      MraidState.HIDDEN -> onHidden.countDown()
    }
    onExpanded.countDown()
  }

  private fun String.toMraidState(): MraidState {
    val unquotedState = this.replace("\"", "")
    return MraidState.values().first { it.stringValue == unquotedState }
  }
}
