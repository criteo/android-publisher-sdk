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

import android.webkit.WebView
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class MraidInteractorTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var webView: WebView
  private lateinit var mraidInteractor: MraidInteractor

  @Before
  fun setUp() {
    mraidInteractor = MraidInteractor(webView)
  }

  @Test
  fun whenNotifyReadyWithInlinePlacement_ShouldEvaluateNotifyReadyOnMraidObject() {
    mraidInteractor.notifyReady(MraidPlacementType.INLINE)

    verify(webView).evaluateJavascript("window.mraid.notifyReady(\"inline\")", null)
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyReadyWithInterstitialPlacement_ShouldEvaluateNotifyReadyOnMraidObject() {
    mraidInteractor.notifyReady(MraidPlacementType.INTERSTITIAL)

    verify(webView).evaluateJavascript("window.mraid.notifyReady(\"interstitial\")", null)
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyError_GivenActionNonNull_ShouldEvaluateNotifyErrorWithActionParamOnMraidObject() {
    mraidInteractor.notifyError("message", "action")

    verify(webView).evaluateJavascript(
        "window.mraid.notifyError(\"message\", \"action\")",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyError_GivenActionIsEmpty_ShouldEvaluateNotifyErrorWithActionParamOnMraidObject() {
    mraidInteractor.notifyError("message", "")

    verify(webView).evaluateJavascript(
        "window.mraid.notifyError(\"message\", \"\")",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyError_GivenActionNull_ShouldEvaluateNotifyErrorWithoutActionParamOnMraidObject() {
    mraidInteractor.notifyError("message", null)

    verify(webView).evaluateJavascript(
        "window.mraid.notifyError(\"message\", undefined)",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenSetIsViewable_GivenIsViewableIsTrue_ShouldEvaluateSetIsViewableOnMraidObject() {
    mraidInteractor.setIsViewable(true)

    verify(webView).evaluateJavascript(
        "window.mraid.setIsViewable(true)",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenSetIsViewable_GivenIsViewableIsFalse_ShouldEvaluateSetIsViewableOnMraidObject() {
    mraidInteractor.setIsViewable(false)

    verify(webView).evaluateJavascript(
        "window.mraid.setIsViewable(false)",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyExpanded_ShouldEvaluateNotifyExpandedOnMraidObject() {
    mraidInteractor.notifyExpanded()

    verify(webView).evaluateJavascript(
        "window.mraid.notifyExpanded()",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyResized_ShouldEvaluateNotifyResizedOnMraidObject() {
    mraidInteractor.notifyResized()

    verify(webView).evaluateJavascript(
        "window.mraid.notifyResized()",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenNotifyClosed_ShouldEvaluateNotifyClosedOnMraidObject() {
    mraidInteractor.notifyClosed()

    verify(webView).evaluateJavascript(
        "window.mraid.notifyClosed()",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenSetMaxSize_ShouldEvaluateSetMaxSizeOnMraidObject() {
    mraidInteractor.setMaxSize(50, 60, 2.1)

    verify(webView).evaluateJavascript(
        "window.mraid.setMaxSize(50, 60, 2.1)",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenSetScreenSize_ShouldEvaluateSetScreenSizeOnMraidObject() {
    mraidInteractor.setScreenSize(123, 456)

    verify(webView).evaluateJavascript(
        "window.mraid.setScreenSize(123, 456)",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenSetSupports_ShouldEvaluateSupportsOnMraidObject() {
    mraidInteractor.setSupports(sms = true, tel = true)

    verify(webView).evaluateJavascript(
        "window.mraid.setSupports({sms: true, tel: true})",
        null
    )
    verifyNoMoreInteractions(webView)
  }

  @Test
  fun whenSetCurrentPosition_ShouldEvaluateSetCurrentPositionOnMraidObject() {
    mraidInteractor.setCurrentPosition(1, 2, 123, 234)

    verify(webView).evaluateJavascript(
        "window.mraid.setCurrentPosition(1, 2, 123, 234)",
        null
    )
    verifyNoMoreInteractions(webView)
  }
}
