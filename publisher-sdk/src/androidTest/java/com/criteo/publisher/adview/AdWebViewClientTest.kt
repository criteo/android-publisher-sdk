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
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.test.activity.DummyActivity
import com.criteo.publisher.view.WebViewClicker
import com.criteo.publisher.view.WebViewLookup
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

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

  @SpyBean
  private lateinit var context: Context

  @Mock
  private lateinit var listener: RedirectionListener
  private lateinit var webView: WebView
  private lateinit var webViewClient: TestAdWebViewClient
  @Mock
  private lateinit var webViewClientListener: AdWebViewClientListener

  @SpyBean
  private lateinit var redirection: Redirection

  @Before
  fun setUp() {
    webViewClient = spy(
        TestAdWebViewClient(
            listener,
            activityRule.activity.componentName
        )
    )
    webView = ThreadingUtil.callOnMainThreadAndWait {
      val view = object : AdWebView(context) {
        override fun provideMraidController(): MraidController {
          return mock()
        }
      }
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
  fun whenUserClickOnAd_GivenTargetAppIsNotInstalled_DontThrowActivityNotFoundAndNotifyOnRedirectionFailed() {
    // We assume that no application can handle such URL.
    whenUserClickOnAd("fake-deeplink://fakeappdispatch")

    verify(context, never()).startActivity(ArgumentMatchers.any())
    verify(listener).onRedirectionFailed()
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
  fun whenDeprecatedShouldInterceptRequestAndListenerIsNull_ShouldNotThrow() {
    assertThatCode {
      webViewClient.shouldInterceptRequest(
          webView,
          "https://www.criteo.com/mraid.js"
      )
    }.doesNotThrowAnyException()
  }

  @Test
  fun whenShouldInterceptRequestAndListenerIsNull_ShouldNotThrow() {
    assertThatCode {
      webViewClient.shouldInterceptRequest(
          webView,
          mock<WebResourceRequest>()
      )
    }.doesNotThrowAnyException()
  }

  @Test
  fun whenShouldInterceptRequestAndListenerIsPresent_ShouldReturnResultFromListener() {
    val url = "https://www.criteo.com/mraid.js"
    val webResourceResponse = mock<WebResourceResponse>()
    val webResourceRequest = mock<WebResourceRequest>()
    whenever(webResourceRequest.url).thenReturn(Uri.parse(url))

    webViewClient.setAdWebViewClientListener(webViewClientListener)
    whenever(webViewClientListener.shouldInterceptRequest(url)).thenReturn(webResourceResponse)

    val result = webViewClient.shouldInterceptRequest(
        webView,
        webResourceRequest
    )

    assertThat(result).isEqualTo(webResourceResponse)
  }

  @Test
  fun whenDeprecatedShouldInterceptRequestAndListenerIsPresent_ShouldReturnResultFromListener() {
    val url = "https://www.criteo.com/mraid.js"
    val webResourceResponse = mock<WebResourceResponse>()

    webViewClient.setAdWebViewClientListener(webViewClientListener)
    whenever(webViewClientListener.shouldInterceptRequest(url)).thenReturn(webResourceResponse)

    val result = webViewClient.shouldInterceptRequest(
        webView,
        url
    )

    assertThat(result).isEqualTo(webResourceResponse)
  }

  @Test
  fun whenOpenIsCalled_ShouldCallRedirectionListener() {
    val url = "https://www.criteo.com/"
    webViewClient.open(url)

    verify(redirection).redirect(eq(url), eq(activityRule.activity.componentName), any())
  }

  @Test
  fun whenOpenIsCalledAndUserRedirectedToAd_ThenShouldCallRedirectionListener() {
    val url = "https://www.criteo.com/"
    doAnswer {
      (it.getArgument(2) as? RedirectionListener)?.onUserRedirectedToAd()
    }.whenever(redirection).redirect(eq(url), eq(activityRule.activity.componentName), any())

    webViewClient.open(url)

    verify(listener).onUserRedirectedToAd()
  }

  @Test
  fun whenOpenIsCalledAndUserBackFromAd_ThenShouldCallRedirectionListener() {
    val url = "https://www.criteo.com/"
    doAnswer {
      (it.getArgument(2) as? RedirectionListener)?.onUserBackFromAd()
    }.whenever(redirection).redirect(eq(url), eq(activityRule.activity.componentName), any())

    webViewClient.open(url)

    verify(listener).onUserBackFromAd()
  }

  @Test
  fun whenOpenIsCalledAndRedirectionFailed_ThenShouldCallRedirectionListener() {
    val url = "https://www.criteo.com/"
    doAnswer {
      (it.getArgument(2) as? RedirectionListener)?.onRedirectionFailed()
    }.whenever(redirection).redirect(eq(url), eq(activityRule.activity.componentName), any())

    webViewClient.open(url)

    verify(listener).onRedirectionFailed()
  }

  private fun whenUserClickOnAd(url: String) {
    clicker.loadHtmlAndSimulateClickOnAd(webView, url)
  }
}
