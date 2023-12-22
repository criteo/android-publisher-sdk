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

import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import com.criteo.publisher.advancednative.VisibilityTracker
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.logging.Logger
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.util.DeviceUtil
import com.criteo.publisher.util.ExternalVideoPlayer
import com.criteo.publisher.util.ViewPositionTracker
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import java.io.IOException
import javax.inject.Inject

class CriteoMraidControllerTest {
  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var adWebView: AdWebView

  @Mock
  private lateinit var visibilityTracker: VisibilityTracker

  @Mock
  private lateinit var mraidInteractor: MraidInteractor

  @Mock
  private lateinit var mraidMessageHandler: MraidMessageHandler

  @Mock
  private lateinit var adWebViewClient: AdWebViewClient

  @Mock
  private lateinit var configuration: Configuration

  @Mock
  private lateinit var resources: Resources

  @Mock
  private lateinit var displayMetrics: DisplayMetrics

  @Mock
  private lateinit var deviceUtil: DeviceUtil

  @Mock
  private lateinit var viewPositionTracker: ViewPositionTracker

  @Mock
  private lateinit var externalVideoPlayer: ExternalVideoPlayer

  @SpyBean
  private lateinit var logger: Logger

  @Inject
  private lateinit var runOnUiThreadExecutor: RunOnUiThreadExecutor

  private var placementType: MraidPlacementType = MraidPlacementType.INLINE
  private var actionResult: MraidActionResult = MraidActionResult.Success
  private lateinit var resizeActionResult: MraidResizeActionResult

  private lateinit var criteoMraidController: CriteoMraidController

  @Before
  fun setUp() {
    criteoMraidController = object : CriteoMraidController(
        adWebView,
        visibilityTracker,
        mraidInteractor,
        mraidMessageHandler,
        deviceUtil,
        viewPositionTracker,
        externalVideoPlayer,
        runOnUiThreadExecutor
    ) {
      override fun getPlacementType(): MraidPlacementType {
        return placementType
      }

      override fun doExpand(
          width: Double,
          height: Double,
          onResult: (result: MraidActionResult) -> Unit
      ) {
        onResult(actionResult)
      }

      override fun doClose(onResult: (result: MraidActionResult) -> Unit) {
        onResult(actionResult)
      }

      override fun doResize(
          width: Double,
          height: Double,
          offsetX: Double,
          offsetY: Double,
          customClosePosition: MraidResizeCustomClosePosition,
          allowOffscreen: Boolean,
          onResult: (result: MraidResizeActionResult) -> Unit
      ) {
        onResult(resizeActionResult)
      }

      override fun doSetOrientationProperties(
          allowOrientationChange: Boolean,
          forceOrientation: MraidOrientation,
          onResult: (result: MraidActionResult) -> Unit
      ) {
        onResult(actionResult)
      }

      override fun resetToDefault() {
        // no-op
      }
    }

    whenever(adWebView.resources).thenReturn(resources)
    whenever(resources.configuration).thenReturn(configuration)
    whenever(resources.displayMetrics).thenReturn(displayMetrics)
    whenever(deviceUtil.getRealScreenSize()).thenReturn(AdSize(100, 100))
  }

  @Test
  fun constructor_ShouldSetupMraidMessageHandler() {
    verify(adWebView).addJavascriptInterface(
        mraidMessageHandler,
        CriteoMraidController.WEB_VIEW_INTERFACE_NAME
    )
    verify(mraidMessageHandler).setListener(criteoMraidController)
  }

  @Test
  fun onVisible_ShouldReportViewabilityToMraidInteractor() {
    criteoMraidController.onVisible()

    verify(mraidInteractor).setIsViewable(true)
  }

  @Test
  fun onVisibleTwoTimes_ShouldReportViewabilityToMraidInteractorOnlyOnce() {
    criteoMraidController.onVisible()
    criteoMraidController.onVisible()

    verify(mraidInteractor).setIsViewable(true)
  }

  @Test
  fun onGone_ShouldReportViewabilityToMraidInteractor() {
    criteoMraidController.onGone()

    verify(mraidInteractor).setIsViewable(false)
  }

  @Test
  fun onGoneTwoTimes_ShouldReportViewabilityToMraidInteractorOnlyOnce() {
    criteoMraidController.onGone()
    criteoMraidController.onGone()

    verify(mraidInteractor).setIsViewable(false)
  }

  @Test
  fun onOpenAndWebViewClientIsSet_ShouldDelegateOpenToAdWebViewClient() {
    val url = "https://www.criteo.com/"

    criteoMraidController.onWebViewClientSet(adWebViewClient)
    criteoMraidController.onOpen(url)

    verify(adWebViewClient).open(url)
  }

  @Test
  fun onOpenAndWebViewClientNotSet_ShouldNotThrow() {
    assertThatCode { criteoMraidController.onOpen("https://www.criteo.com/") }
        .doesNotThrowAnyException()
  }

  @Test
  fun onExpandWithSuccess_ShouldNotifyMraidInteractorAboutExpandedAndChangeCurrentState() {
    criteoMraidController.onExpand(100.0, 100.0)

    verify(mraidInteractor).notifyExpanded()
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.EXPANDED)
  }

  @Test
  fun onExpandWithError_ShouldNotifyMraidInteractorAboutError() {
    val errorResult = MraidActionResult.Error("message", "action")
    actionResult = errorResult

    criteoMraidController.onExpand(100.0, 100.0)

    verify(mraidInteractor).notifyError(errorResult.message, errorResult.action)
    verify(mraidInteractor, never()).notifyExpanded()
  }

  @Test
  fun onCloseWithSuccessAndDefaultState_ShouldNotifyMraidInteractorAboutCloseAndChangeCurrentState() {
    givenMraidAdAndPageIsFinished()

    criteoMraidController.onClose()

    verify(mraidInteractor).notifyClosed()
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.HIDDEN)
  }

  @Test
  fun onCloseWithSuccessAndExpandState_ShouldNotifyMraidInteractorAboutCloseAndChangeCurrentState() {
    givenMraidAdAndPageIsFinished()
    criteoMraidController.onExpand(100.0, 100.0)

    criteoMraidController.onClose()

    verify(mraidInteractor).notifyClosed()
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.DEFAULT)
  }

  @Test
  fun onCloseWithError_ShouldNotifyMraidInteractorAboutError() {
    givenMraidAdAndPageIsFinished()
    val errorResult = MraidActionResult.Error("message", "action")
    actionResult = errorResult

    criteoMraidController.onClose()

    verify(mraidInteractor).notifyError(errorResult.message, errorResult.action)
    verify(mraidInteractor, never()).notifyClosed()
  }

  @Test
  fun onResizeWithSuccess_ShouldNotifyMraidInteractorAboutResizeAndChangeCurrentState() {
    resizeActionResult = MraidResizeActionResult.Success(0, 0, 100, 100)

    criteoMraidController.onResize(
        100.0,
        100.0,
        0.0,
        0.0,
        MraidResizeCustomClosePosition.CENTER,
        true
    )

    verify(mraidInteractor).notifyResized()
    verify(mraidInteractor).setCurrentPosition(0, 0, 100, 100)
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.RESIZED)
  }

  @Test
  fun onResizeWithError_ShouldNotifyMraidInteractorAboutError() {
    val errorResult = MraidResizeActionResult.Error("message", "action")
    resizeActionResult = errorResult

    criteoMraidController.onResize(
        100.0,
        100.0,
        0.0,
        0.0,
        MraidResizeCustomClosePosition.CENTER,
        true
    )

    verify(mraidInteractor).notifyError(errorResult.message, errorResult.action)
    verify(mraidInteractor, never()).notifyResized()
  }

  @Test
  fun onSetOrientationPropertiesWithError_ShouldNotifyMraidInteractorAboutError() {
    val errorResult = MraidActionResult.Error("message", "action")
    actionResult = errorResult

    criteoMraidController.onSetOrientationProperties(true, MraidOrientation.LANDSCAPE)

    verify(mraidInteractor).notifyError(errorResult.message, errorResult.action)
  }

  @Test
  fun onSetOrientationPropertiesWithSuccess_ShouldNotThrow() {
    assertThatCode {
      criteoMraidController.onSetOrientationProperties(true, MraidOrientation.LANDSCAPE)
    }.doesNotThrowAnyException()
  }

  @Test
  fun onPageFinishedGivenMraidAd_ShouldInitializeDefaultValues() {
    whenever(deviceUtil.canSendSms()).thenReturn(true)
    whenever(deviceUtil.canInitiateCall()).thenReturn(false)
    givenMraidAdAndPageIsFinished()

    verify(visibilityTracker).watch(adWebView, criteoMraidController)
    verify(viewPositionTracker).watch(adWebView, criteoMraidController)

    val inOrder = inOrder(mraidInteractor)
    inOrder.verify(mraidInteractor).setMaxSize(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        adWebView.resources.displayMetrics.density.toDouble()
    )
    inOrder.verify(mraidInteractor).setScreenSize(100, 100)
    inOrder.verify(mraidInteractor).setSupports(sms = true, tel = false)
    inOrder.verify(mraidInteractor).notifyReady(criteoMraidController.getPlacementType())

    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.DEFAULT)
  }

  @Test
  fun onPageFinishedGivenNonMraidAd_ShouldNotInitializeDefaultValues() {
    givenNonMraidAdAndPageIsFinished()

    verifyZeroInteractions(visibilityTracker)
    verifyZeroInteractions(mraidInteractor)
  }

  @Test
  fun onOpenFailed_GivenMraidAd_ShouldNotifyMraidInteractorAboutError() {
    givenMraidAdAndPageIsFinished()

    criteoMraidController.onOpenFailed()

    verify(mraidInteractor).notifyError(any(), anyOrNull())
  }

  @Test
  fun onOpenFailed_GivenNonMraidAd_ShouldNotifyMraidInteractorAboutError() {
    givenNonMraidAdAndPageIsFinished()

    criteoMraidController.onOpenFailed()

    verifyZeroInteractions(mraidInteractor)
  }

  @Test
  fun shouldInterceptRequest_GivenMraidUrl_ShouldReturnWebResourceResponse() {
    val resource = criteoMraidController.shouldInterceptRequest("https://www.criteo.com/mraid.js")

    assertThat(resource).isNotNull
  }

  @Test
  fun shouldInterceptRequest_GivenNonMraidUrl_ShouldReturnNull() {
    val resource = criteoMraidController.shouldInterceptRequest("https://www.criteo.com/somerandomfile.js")

    assertThat(resource).isNull()
  }

  @Test
  fun shouldInterceptRequest_GivenMraidUrlAndAssetsThrowException_ShouldReturnNull() {
    val exception = IOException("Something went wrong")
    whenever(adWebView.context.assets.open(anyOrNull())).thenThrow(exception)

    val resource = criteoMraidController.shouldInterceptRequest("https://www.criteo.com/mraid.js")

    verify(logger).log(MraidLogMessage.onErrorDuringMraidFileInject(exception))
    assertThat(resource).isNull()
  }

  @Test
  fun onWebViewClientSetGivenClientIsAdWebViewClient_ShouldSetListenerOnIt() {
    criteoMraidController.onWebViewClientSet(adWebViewClient)

    verify(adWebViewClient).setAdWebViewClientListener(criteoMraidController)
  }

  @Test
  fun onWebViewClientSetGivenClientIsNotAdWebViewClient_ShouldNotThrow() {
    assertThatCode {
      criteoMraidController.onWebViewClientSet(mock())
    }.doesNotThrowAnyException()
  }

  @Test
  fun onConfigurationChangeGivenConfigIsNotNullAndMraidAd_ShouldSetMaxSizeOnMraidInteractor() {
    givenMraidAdAndPageIsFinished()

    criteoMraidController.onConfigurationChange(configuration)

    // first time for init
    // second time for onConfigurationChange call
    verify(mraidInteractor, times(2)).setMaxSize(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        adWebView.resources.displayMetrics.density.toDouble()
    )

    verify(mraidInteractor, times(2)).setScreenSize(
        100,
        100
    )
  }

  @Test
  fun onConfigurationChangeGivenConfigIsNotNullAndAdIsNotMraid_ShouldNotThrow() {
    givenNonMraidAdAndPageIsFinished()

    assertThatCode {
      criteoMraidController.onConfigurationChange(configuration)
    }.doesNotThrowAnyException()

    verifyZeroInteractions(mraidInteractor)
  }

  @Test
  fun onConfigurationChangeGivenConfigIsNullAndMraidAd_ShouldNotThrow() {
    givenMraidAdAndPageIsFinished()

    assertThatCode {
      criteoMraidController.onConfigurationChange(null)
    }.doesNotThrowAnyException()

    // first time for init
    verify(mraidInteractor).setMaxSize(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        adWebView.resources.displayMetrics.density.toDouble()
    )
  }

  @Test
  fun onConfigurationChangeGivenConfigIsNullAndndAdIsNotMraid_ShouldNotThrow() {
    givenNonMraidAdAndPageIsFinished()

    assertThatCode {
      criteoMraidController.onConfigurationChange(null)
    }.doesNotThrowAnyException()

    verifyZeroInteractions(mraidInteractor)
  }

  @Test
  fun onClosedGivenAdIsMraidAndDefaultState_ShouldNotifyMraidInteractorAboutCloseAndChangeCurrentState() {
    givenMraidAdAndPageIsFinished()

    criteoMraidController.onClosed()

    verify(mraidInteractor).notifyClosed()
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.HIDDEN)
  }

  @Test
  fun onClosedGivenAdIsMraidAndExpandedState_ShouldNotifyMraidInteractorAboutCloseAndChangeCurrentState() {
    givenMraidAdAndPageIsFinished()
    criteoMraidController.onExpand(100.0, 100.0)

    criteoMraidController.onClosed()

    verify(mraidInteractor).notifyClosed()
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.DEFAULT)
  }

  @Test
  fun onClosedGivenAdIsMraidAndHiddenState_ShouldKeepTheSameState() {
    givenMraidAdAndPageIsFinished()
    criteoMraidController.onClose()

    criteoMraidController.onClosed()

    // single interaction from first close
    verify(mraidInteractor).notifyClosed()
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.HIDDEN)
  }

  @Test
  fun onClosedGivenAdIsNotMraid_ShouldNotThrowKeepTheLoadingState() {
    givenNonMraidAdAndPageIsFinished()

    assertThatCode {
      criteoMraidController.onClosed()
    }.doesNotThrowAnyException()

    verifyZeroInteractions(mraidInteractor)
    assertThat(criteoMraidController.currentState).isEqualTo(MraidState.LOADING)
  }

  @Test
  fun onPositionChange_ShouldDelegateToMraidInteractor() {
    criteoMraidController.onPositionChange(1, 2, 123, 234)

    verify(mraidInteractor).setCurrentPosition(1, 2, 123, 234)
  }

  @Test
  fun onPlayVideo_GivenNoError_ShouldDelegateCallToExternalVideoPlayer() {
    criteoMraidController.onPlayVideo("https://criteo.com/cat_video.mp4")

    verify(externalVideoPlayer).play(eq("https://criteo.com/cat_video.mp4"), any())
    verifyZeroInteractions(mraidInteractor)
  }

  @Test
  fun onPlayVideo_GivenErrorInExternalVideoPlayer_ShouldDelegateErrorToMraidInteractor() {
    whenever(externalVideoPlayer.play(eq("https://criteo.com/cat_video.mp4"), any())).thenAnswer {
      (it.arguments[1] as (String) -> Unit).invoke("error message")
    }
    criteoMraidController.onPlayVideo("https://criteo.com/cat_video.mp4")

    verify(mraidInteractor).notifyError("error message", "playVideo")
  }

  private fun givenMraidAdAndPageIsFinished() {
    criteoMraidController.onWebViewClientSet(adWebViewClient)
    criteoMraidController.shouldInterceptRequest("https://www.criteo.com/mraid.js")
    criteoMraidController.onPageFinished()
  }

  private fun givenNonMraidAdAndPageIsFinished() {
    criteoMraidController.onWebViewClientSet(adWebViewClient)
    criteoMraidController.onPageFinished()
  }
}
