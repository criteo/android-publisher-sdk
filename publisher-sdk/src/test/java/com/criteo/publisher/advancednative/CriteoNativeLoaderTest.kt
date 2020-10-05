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

package com.criteo.publisher.advancednative

import com.criteo.publisher.BidListener
import com.criteo.publisher.BidManager
import com.criteo.publisher.CriteoErrorCode
import com.criteo.publisher.InHouse
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.model.Config
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import javax.inject.Inject

@RunWith(Parameterized::class)
class CriteoNativeLoaderTest(private val liveBiddingEnabled: Boolean) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "liveBiddingEnabled_{0}")
    fun data(): Collection<Array<out Any>> {
      return listOf(arrayOf(true), arrayOf(false))
    }
  }

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var config: Config

  @SpyBean
  private lateinit var inHouse: InHouse

  @SpyBean
  private lateinit var bidManager: BidManager

  @MockBean
  private lateinit var nativeAdMapper: NativeAdMapper

  @MockBean
  private lateinit var defaultImageLoader: ImageLoader

  @MockBean
  private lateinit var integrationRegistry: IntegrationRegistry

  @Inject
  private lateinit var imageLoaderHolder: ImageLoaderHolder

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  private lateinit var adUnit: NativeAdUnit

  @Mock
  private lateinit var listener: CriteoNativeAdListener

  @Mock
  private lateinit var renderer: CriteoNativeRenderer

  private lateinit var runOnUiThreadExecutor: DirectMockRunOnUiThreadExecutor

  private lateinit var nativeLoader: CriteoNativeLoader

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    doReturn(liveBiddingEnabled).whenever(config).isLiveBiddingEnabled

    runOnUiThreadExecutor = DirectMockRunOnUiThreadExecutor()
    mockedDependenciesRule.dependencyProvider.stub {
      on { provideRunOnUiThreadExecutor() } doReturn runOnUiThreadExecutor
    }

    adUnit = NativeAdUnit("myAdUnit")
    nativeLoader = CriteoNativeLoader(adUnit, listener, renderer)
  }

  @Test
  fun setImageLoader_GivenLoader_SetItInTheHolder() {
    val customImageLoader = mock<ImageLoader>()

    val previousImageLoader = imageLoaderHolder.get()
    CriteoNativeLoader.setImageLoader(customImageLoader)

    assertThat(previousImageLoader).isSameAs(defaultImageLoader)
    assertThat(imageLoaderHolder.get()).isSameAs(customImageLoader)
  }

  @Test
  fun loadAdInHouse_GivenNotANativeBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNotANativeBidAvailable()

    inHouse.loadBid(adUnit, nativeLoader::loadAd)

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    verify(integrationRegistry).declare(Integration.IN_HOUSE)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAdInHouse_GivenANullBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNotANativeBidAvailable()

    nativeLoader.loadAd(null)

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    verify(integrationRegistry).declare(Integration.IN_HOUSE)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAdInHouse_GivenNativeBid_NotifyListenerOnMainThreadForSuccess() {
    expectListenerToBeCalledOnUiThread()
    val nativeAd = givenNativeBidAvailable()

    inHouse.loadBid(adUnit, nativeLoader::loadAd)

    verify(listener).onAdReceived(nativeAd)
    verifyNoMoreInteractions(listener)
    verify(integrationRegistry).declare(Integration.IN_HOUSE)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenNoBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNoBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    verify(integrationRegistry).declare(Integration.STANDALONE)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenNativeBid_NotifyListenerOnUiThreadForSuccess() {
    expectListenerToBeCalledOnUiThread()
    val nativeAd = givenNativeBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdReceived(nativeAd)
    verifyNoMoreInteractions(listener)
    verify(integrationRegistry).declare(Integration.STANDALONE)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenNotANativeBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNotANativeBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    verify(integrationRegistry).declare(Integration.STANDALONE)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenAnythingThrowingException_ExceptionIsCatch() {
    buildConfigWrapper.stub {
      on { preconditionThrowsOnException() } doReturn false
    }

    givenExceptionWhileFetchingBid()

    // then
    assertThatCode {
      nativeLoader.loadAd()
    }.doesNotThrowAnyException()
  }

  private fun givenExceptionWhileFetchingBid() {
    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(any()) } doThrow RuntimeException::class
    }
  }

  private fun givenNoBidAvailable() {
    bidManager.stub {
      doReturn(null).whenever(mock).getBidForAdUnitAndPrefetch(adUnit)

      doAnswer {
        it.getArgument<BidListener>(1).onNoBid()
      }.whenever(mock).getLiveBidForAdUnit(eq(adUnit), any())
    }
  }

  private fun givenNativeBidAvailable(): CriteoNativeAd {
    val nativeAssets = mock<NativeAssets>()
    val nativeAd = mock<CriteoNativeAd>()
    val slot = mock<CdbResponseSlot>() {
      on { this.nativeAssets } doReturn nativeAssets
    }

    bidManager.stub {
      doReturn(slot).whenever(mock).getBidForAdUnitAndPrefetch(adUnit)

      doAnswer {
        it.getArgument<BidListener>(1).onBidResponse(slot)
      }.whenever(mock).getLiveBidForAdUnit(eq(adUnit), any())
    }

    nativeAdMapper.stub {
      on { map(eq(nativeAssets), argThat { listener == get() }, any()) } doReturn nativeAd
    }
    return nativeAd
  }

  private fun givenNotANativeBidAvailable() {
    val slot = mock<CdbResponseSlot>() {
      on { nativeAssets } doReturn null
    }

    bidManager.stub {
      doReturn(slot).whenever(mock).getBidForAdUnitAndPrefetch(adUnit)

      doAnswer {
        it.getArgument<BidListener>(1).onBidResponse(slot)
      }.whenever(mock).getLiveBidForAdUnit(eq(adUnit), any())
    }
  }

  private fun expectListenerToBeCalledOnUiThread() {
    listener.stub {
      on { onAdFailedToReceive(any()) } doAnswer {
        runOnUiThreadExecutor.expectIsRunningInExecutor()
        null
      }

      on { onAdReceived(any()) } doAnswer {
        runOnUiThreadExecutor.expectIsRunningInExecutor()
        null
      }
    }
  }
}
