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
import com.criteo.publisher.callMraidObjectBlocking
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.loadMraidHtml
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.test.activity.DummyActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.spy

class MraidIntegrationTest {

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

  @SpyBean
  private lateinit var context: Context

  @Mock
  private lateinit var listener: RedirectionListener
  private lateinit var webView: AdWebView
  private lateinit var webViewClient: TestAdWebViewClient

  private val mraidData = MraidData()

  @Before
  fun setUp() {
    webViewClient = spy(
        TestAdWebViewClient(
            listener,
            activityRule.activity.componentName
        )
    )
    webView = ThreadingUtil.callOnMainThreadAndWait {
      val view = AdWebView(context)
      view.settings.javaScriptEnabled = true
      view.webViewClient = webViewClient
      view
    }
  }

  @Test
  fun whenHtmlWithMraidIsLoaded_GivenPageFinished_ShouldHaveMraidStateAsDefault() {
    val mraidHtml = mraidData.getHtmlWithMraidScriptTag()

    webView.loadMraidHtml(mraidHtml)
    webViewClient.waitForPageToFinishLoading()

    val state = webView.callMraidObjectBlocking("getState()")
    assertThat(state).isEqualTo("\"default\"")
  }

  @Test
  fun whenHtmlWithMraidIsLoaded_GivenViewIsVisible_ShouldHaveIsViewableAsTrue() {
    val mraidHtml = mraidData.getHtmlWithMraidScriptTag()

    webView.loadMraidHtml(mraidHtml)
    webViewClient.waitForPageToFinishLoading()
    ThreadingUtil.callOnMainThreadAndWait {
      webView.onVisible()
    }

    val isViewable = webView.callMraidObjectBlocking("isViewable()")
    assertThat(isViewable).isEqualTo("true")

  }
}
