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

package com.criteo.publisher.adview

import android.content.Context
import android.webkit.WebView
import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.MraidData
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.logging.Logger
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.test.activity.DummyActivity
import com.criteo.publisher.util.CompletableFuture
import com.criteo.publisher.view.WebViewClicker
import com.criteo.publisher.view.WebViewLookup
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnit
import java.io.IOException

class AdWebViewClientTest {

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

  private val lookup = WebViewLookup()
  private val clicker = WebViewClicker()
  private val mraidData = MraidData()

  @SpyBean
  private lateinit var context: Context

  @MockBean
  private lateinit var logger: Logger

  @Mock
  private lateinit var listener: RedirectionListener
  private lateinit var webView: WebView
  private lateinit var webViewClient: AdWebViewClient
  private val pageFinishedLoading = CompletableFuture<Unit>()

  @Before
  fun setUp() {
    webViewClient = spy(
        object : AdWebViewClient(
            listener,
            activityRule.activity.componentName
        ) {
          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            pageFinishedLoading.complete(Unit)
          }
        }
    )
    webView = ThreadingUtil.callOnMainThreadAndWait {
      val view = WebView(context)
      view.settings.javaScriptEnabled = true
      view.webViewClient = webViewClient
      view
    }
  }

  @Test
  fun whenUserClickOnAd_GivenHttpUrl_OpenActivityAndNotifyListener() {
    // We assume that there is a browser installed on the test device.
    whenUserClickOnAd("https://criteo.com")

    verify(context).startActivity(ArgumentMatchers.any())
    verify(listener).onUserRedirectedToAd()
    verifyNoMoreInteractions(listener)
  }

  @Test
  fun whenUserClickOnAd_GivenDeepLinkAndInstalledAppToHandleIt_OpenActivityAndNotifyListener() {
    whenUserClickOnAd("criteo-test://dummy-ad-activity")

    verify(context).startActivity(ArgumentMatchers.any())
    verify(listener).onUserRedirectedToAd()
    verifyNoMoreInteractions(listener)
  }

  @Test
  fun whenUserClickOnAd_GivenTargetAppIsNotInstalled_DontThrowActivityNotFoundAndDoNotRedirectUser() {
    // We assume that no application can handle such URL.
    whenUserClickOnAd("fake-deeplink://fakeappdispatch")

    verify(context, never()).startActivity(ArgumentMatchers.any())
    verifyNoMoreInteractions(listener)
  }

  @Test
  fun whenUserClickOnAdAndGoBack_GivenDeepLinkAndInstalledAppToHandleIt_NotifyListener() {
    val activity = lookup.lookForResumedActivity {
      whenUserClickOnAd(
          "criteo-test://dummy-ad-activity"
      )
    }.get()

    lookup.lookForResumedActivity {
      runOnMainThreadAndWait { activity.onBackPressed() }
    }.get()

    val inOrder = inOrder(listener)
    inOrder.verify(listener).onUserRedirectedToAd()
    inOrder.verify(listener).onUserBackFromAd()
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun whenHtmlWithMraidIsLoaded_GivenMraidInScriptTag_ShouldLoadMraidScript() {
    val mraidHtml = mraidData.getHtmlWithMraidScriptTag()

    loadHtml(mraidHtml)
    waitForPageToFinishLoading()

    verifyMraidObjectAvailable()
  }

  @Test
  fun whenHtmlWithMraidIsLoaded_GivenMraidViaDocumentWrite_ShouldLoadMraidScript() {
    val mraidHtml = mraidData.getHtmlWithDocumentWriteMraidScriptTag()

    loadHtml(mraidHtml)
    waitForPageToFinishLoading()

    verifyMraidObjectAvailable()
  }

  @Test
  fun whenHtmlWithoutMraidIsLoaded_GivenHtmlWithoutMraidScript_ShouldNotLoadMraidObject() {
    val html = mraidData.getHtmlWithoutMraidScript()

    loadHtml(html)
    waitForPageToFinishLoading()

    verifyMraidObjectNotAvailable()
  }

  @Test
  fun whenHtmlWithMraidIsLoaded_GivenAssetsThrowIOException_ShouldLogError() {
    val mraidHtml = mraidData.getHtmlWithDocumentWriteMraidScriptTag()
    val thrownException = IOException()
    whenever(context.assets.open("criteo-mraid.js")).thenAnswer { throw thrownException }

    loadHtml(mraidHtml)
    waitForPageToFinishLoading()

    verify(logger).log(MraidLogMessage.onErrorDuringMraidFileInject(thrownException))
  }

  private fun loadHtml(html: String) {
    runOnMainThreadAndWait {
      webView.loadDataWithBaseURL(
          "https://www.criteo.com",
          html,
          "text/html",
          "UTF-8",
          ""
      )
    }
  }

  private fun verifyMraidObjectAvailable() {
    verifyMraidObject { result ->
      assertThat(result).isNotEqualTo("null")
    }
  }

  private fun verifyMraidObjectNotAvailable() {
    verifyMraidObject { result ->
      assertThat(result).isEqualTo("null")
    }
  }

  private fun verifyMraidObject(assertion: (String) -> Unit) {
    val mraidResult = CompletableFuture<String>()
    runOnMainThreadAndWait {
      webView.evaluateJavascript(
          "javascript:window.mraid",
          mraidResult::complete
      )
    }
    assertion(mraidResult.get())

  }

  private fun waitForPageToFinishLoading() {
    pageFinishedLoading.get()
  }

  private fun whenUserClickOnAd(url: String) {
    clicker.loadHtmlAndSimulateClickOnAd(webView, url)
  }
}
