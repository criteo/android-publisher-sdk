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

package com.criteo.publisher.interstitial

import android.content.Context
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.adview.AdWebViewClient
import com.criteo.publisher.adview.MraidController
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.mock.MockedDependenciesRule
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InterstitialAdWebViewTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var mraidController: MraidController

  @Mock
  private lateinit var listener: () -> Unit

  private lateinit var interstitialAdWebView: InterstitialAdWebView

  @Before
  fun setUp() {
    interstitialAdWebView = InterstitialAdWebView(context, null)

    doReturn(mraidController).whenever(DependencyProvider.getInstance())
        .provideMraidController(MraidPlacementType.INTERSTITIAL, interstitialAdWebView)
  }

  @Test
  fun provideMraidController_ShouldDelegateToDependencyProviderWithInterstitialType() {
    interstitialAdWebView.provideMraidController()

    verify(DependencyProvider.getInstance()).provideMraidController(
        MraidPlacementType.INTERSTITIAL,
        interstitialAdWebView
    )
  }

  @Test
  fun setOnCloseRequestedListenerAndRequestClose_ShouldCallListener() {
    interstitialAdWebView.setOnCloseRequestedListener(listener)

    interstitialAdWebView.requestClose()

    verify(listener).invoke()
  }

  @Test
  fun requestCloseWithoutListener_ShouldNotThrow() {
    assertThatCode {
      interstitialAdWebView.requestClose()
    }.doesNotThrowAnyException()
  }

  @Test
  fun onClosed_ShouldCallOnClosedOnMraidInteractor() {
    interstitialAdWebView.webViewClient = mock<AdWebViewClient>()
    interstitialAdWebView.onClosed()

    verify(mraidController).onClosed()
  }
}
