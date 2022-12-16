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
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.test.activity.DummyActivity
import com.criteo.publisher.view.WebViewClicker
import com.criteo.publisher.view.WebViewLookup
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
import org.mockito.junit.MockitoJUnit

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
  val mockedDependenciesRule = MockedDependenciesRule()

  private val lookup = WebViewLookup()
  private val clicker = WebViewClicker()

  @SpyBean
  private lateinit var context: Context

  @Mock
  private lateinit var listener: RedirectionListener
  private lateinit var webView: WebView
  private lateinit var webViewClient: AdWebViewClient

  @Before
  fun setUp() {
    webViewClient = spy(
        AdWebViewClient(
            listener,
            activityRule.activity.componentName
        )
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

  private fun whenUserClickOnAd(url: String) {
    clicker.loadHtmlAndSimulateClickOnAd(webView, url)
  }
}
