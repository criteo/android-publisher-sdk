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

import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.adview.MraidActionResult
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.adview.MraidResizeActionResult
import com.criteo.publisher.adview.MraidResizeCustomClosePosition
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.test.activity.DummyActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.inject.Inject

class CriteoInterstitialMraidControllerTest {

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

  @Inject
  private lateinit var runOnUiThreadExecutor: RunOnUiThreadExecutor

  @Mock
  private lateinit var interstitialAdWebView: InterstitialAdWebView

  private lateinit var criteoInterstitialMraidController: CriteoInterstitialMraidController

  @Before
  fun setUp() {
    criteoInterstitialMraidController = spy(
        CriteoInterstitialMraidController(
            interstitialAdWebView,
            runOnUiThreadExecutor,
            mock(),
            mock(),
            mock(),
            mock(),
            mock(),
            mock()
        )
    )
  }

  @Test
  fun getPlacementType_ShouldReturnInterstitial() {
    assertThat(criteoInterstitialMraidController.getPlacementType()).isEqualTo(MraidPlacementType.INTERSTITIAL)
  }

  @Test
  fun doExpand_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()

    criteoInterstitialMraidController.doExpand(100.0, 100.0, callbackMock)
    mockedDependenciesRule.waitForIdleState()

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doResize_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidResizeActionResult) -> Unit>()

    criteoInterstitialMraidController.doResize(
        100.0,
        100.0,
        0.0,
        0.0,
        MraidResizeCustomClosePosition.CENTER,
        true,
        callbackMock
    )
    mockedDependenciesRule.waitForIdleState()

    verify(callbackMock).invoke(argThat { this is MraidResizeActionResult.Error })
  }

  @Test
  fun doClose_givenLoadingState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(criteoInterstitialMraidController.currentState).thenReturn(MraidState.LOADING)

    criteoInterstitialMraidController.doClose(callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doClose_givenHiddenState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(criteoInterstitialMraidController.currentState).thenReturn(MraidState.HIDDEN)

    criteoInterstitialMraidController.doClose(callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doClose_givenExpandedState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(criteoInterstitialMraidController.currentState).thenReturn(MraidState.EXPANDED)

    criteoInterstitialMraidController.doClose(callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doClose_givenDefaultState_ShouldRequestCloseOnInterstitialWebViewAndCallbackResult() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(criteoInterstitialMraidController.currentState).thenReturn(MraidState.DEFAULT)

    criteoInterstitialMraidController.doClose(callbackMock)

    verify(interstitialAdWebView).requestClose()
    verify(callbackMock).invoke(argThat { this is MraidActionResult.Success })
  }
}
