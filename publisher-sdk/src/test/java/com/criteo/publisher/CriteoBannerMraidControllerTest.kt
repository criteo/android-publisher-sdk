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

package com.criteo.publisher

import com.criteo.publisher.adview.MraidActionResult
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.criteo.publisher.mock.MockedDependenciesRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CriteoBannerMraidControllerTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var criteoBannerAdWebView: CriteoBannerAdWebView

  private lateinit var bannerMraidController: CriteoBannerMraidController

  private val runOnUiThreadExecutor = DirectMockRunOnUiThreadExecutor()

  @Before
  fun setUp() {
    bannerMraidController = spy(
        CriteoBannerMraidController(
            criteoBannerAdWebView,
            runOnUiThreadExecutor,
            mock(),
            mock(),
            mock(),
            mock(),
            mock()
        )
    )
  }

  @Test
  fun getPlacementType_ShouldReturnInline() {
    assertThat(bannerMraidController.getPlacementType()).isEqualTo(MraidPlacementType.INLINE)
  }

  @Test
  fun doExpand_givenLoadingState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(bannerMraidController.currentState).thenReturn(MraidState.LOADING)

    bannerMraidController.doExpand(100.0, 100.0, callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doExpand_givenHiddenState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(bannerMraidController.currentState).thenReturn(MraidState.HIDDEN)

    bannerMraidController.doExpand(100.0, 100.0, callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doExpand_givenExpandState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(bannerMraidController.currentState).thenReturn(MraidState.EXPANDED)

    bannerMraidController.doExpand(100.0, 100.0, callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doClose_givenLoadingState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(bannerMraidController.currentState).thenReturn(MraidState.LOADING)

    bannerMraidController.doClose(callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }

  @Test
  fun doClose_givenHiddenState_ShouldCallbackError() {
    val callbackMock = mock<(result: MraidActionResult) -> Unit>()
    whenever(bannerMraidController.currentState).thenReturn(MraidState.HIDDEN)

    bannerMraidController.doClose(callbackMock)

    verify(callbackMock).invoke(argThat { this is MraidActionResult.Error })
  }
}
