package com.criteo.publisher.advancednative

import com.criteo.publisher.BidManager
import com.criteo.publisher.CriteoErrorCode
import com.criteo.publisher.InHouse
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.model.Slot
import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CriteoNativeLoaderTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var inHouse: InHouse

  @MockBean
  private lateinit var bidManager: BidManager

  @MockBean
  private lateinit var nativeAdMapper: NativeAdMapper

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

    runOnUiThreadExecutor = DirectMockRunOnUiThreadExecutor()
    mockedDependenciesRule.dependencyProvider.stub {
      on { provideRunOnUiThreadExecutor() } doReturn runOnUiThreadExecutor
    }

    adUnit = NativeAdUnit("myAdUnit")
    nativeLoader = CriteoNativeLoader(adUnit, listener, renderer)
  }

  @Test
  fun loadAdInHouse_GivenNoBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()

    nativeLoader.loadAd(null)

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAdInHouse_GivenNotANativeBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNotANativeBidAvailable()

    val bidResponse = inHouse.getBidResponse(adUnit)
    nativeLoader.loadAd(bidResponse.bidToken)

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAdInHouse_GivenNativeBid_NotifyListenerOnMainThreadForSuccess() {
    expectListenerToBeCalledOnUiThread()
    val nativeAd = givenNativeBidAvailable()

    val bidResponse = inHouse.getBidResponse(adUnit)
    nativeLoader.loadAd(bidResponse.bidToken)

    verify(listener).onAdReceived(nativeAd)
    verifyNoMoreInteractions(listener)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenNoBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNoBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenNativeBid_NotifyListenerOnUiThreadForSuccess() {
    expectListenerToBeCalledOnUiThread()
    val nativeAd = givenNativeBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdReceived(nativeAd)
    verifyNoMoreInteractions(listener)
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun loadAd_GivenNotANativeBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNotANativeBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
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
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn null
    }
  }

  private fun givenNativeBidAvailable(): CriteoNativeAd {
    val nativeAssets = mock<NativeAssets>()
    val nativeAd = mock<CriteoNativeAd>()
    val slot = mock<Slot>() {
      on { this.nativeAssets } doReturn nativeAssets
    }

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn slot
    }

    nativeAdMapper.stub {
      on { map(eq(nativeAssets), argThat { listener == get() }, eq(renderer)) } doReturn nativeAd
    }
    return nativeAd
  }

  private fun givenNotANativeBidAvailable() {
    val slot = mock<Slot>() {
      on { nativeAssets } doReturn null
    }

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn slot
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