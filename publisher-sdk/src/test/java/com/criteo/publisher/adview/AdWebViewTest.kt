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
import android.content.res.Configuration
import android.util.AttributeSet
import com.criteo.publisher.mock.MockedDependenciesRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

class AdWebViewTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Mock
  private lateinit var context: Context

  private lateinit var webView: TestAdWebView

  @Mock
  private lateinit var adWebViewClient: AdWebViewClient

  @Mock
  private lateinit var mraidController: MraidController

  @Before
  fun setUp() {
    webView = spy(TestAdWebView(context, null, mraidController))
  }

  @Test
  fun setWebViewClient_ShouldCreateNewMraidControllerAndCallSetWebViewClientOnMraidController() {
    webView.webViewClient = adWebViewClient

    val inOrder = inOrder(webView, mraidController)
    inOrder.verify(webView).provideMraidController()
    inOrder.verify(mraidController).onWebViewClientSet(adWebViewClient)
  }

  @Test
  fun onConfigurationChange_ShouldCallOnConfigurationChangeOnMraidController() {
    val configuration = Configuration()
    webView.webViewClient = adWebViewClient
    webView.callOnConfigurationChange(configuration)

    verify(mraidController).onConfigurationChange(configuration)
  }

  private open class TestAdWebView(
      context: Context,
      attrs: AttributeSet?,
      val controller: MraidController
  ) : AdWebView(context, attrs) {
    override fun provideMraidController(): MraidController {
      return controller
    }

    fun callOnConfigurationChange(newConfiguration: Configuration?) {
      onConfigurationChanged(newConfiguration)
    }
  }
}
