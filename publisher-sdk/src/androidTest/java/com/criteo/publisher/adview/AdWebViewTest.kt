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
import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.test.activity.DummyActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import javax.inject.Inject

class AdWebViewTest {

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

  @Inject
  private lateinit var context: Context

  private lateinit var webView: AdWebView
  @Mock
  private lateinit var adWebViewClient: AdWebViewClient
  @MockBean
  private lateinit var mraidInteractor: MraidInteractor

  @Before
  fun setUp() {
    webView = ThreadingUtil.callOnMainThreadAndWait {
      val view = spy(AdWebView(context))
      view.settings.javaScriptEnabled = true
      view.webViewClient = adWebViewClient
      view
    }
  }

  @Test
  fun whenSetAdWebViewClient_ShouldSetAdWebViewClientListenerOnIt() {
    verify(adWebViewClient).setAdWebViewClientListener(webView)
  }

  @Test
  fun whenOnPageFinished_ShouldDelegateToMraidInteractor() {
    webView.onPageFinished()

    verify(mraidInteractor).notifyReady()
    verifyNoMoreInteractions(mraidInteractor)
  }
}
