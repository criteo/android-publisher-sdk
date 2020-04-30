package com.criteo.publisher.advancednative

import com.criteo.publisher.BidManager
import com.criteo.publisher.CriteoErrorCode
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.model.Slot
import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.publisher.util.DirectMockRunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
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

  @MockBean
  private lateinit var bidManager: BidManager

  @MockBean
  private lateinit var nativeAdMapper: NativeAdMapper

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  private lateinit var adUnit: NativeAdUnit

  @Mock
  private lateinit var listener: CriteoNativeAdListener

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
    nativeLoader = CriteoNativeLoader(adUnit, listener)
  }

  @Test
  fun loadAd_GivenNoBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNoBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
  }

  @Test
  fun loadAd_GivenNativeBid_NotifyListenerOnUiThreadForSuccess() {
    expectListenerToBeCalledOnUiThread()
    val nativeAd = givenNativeBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdReceived(nativeAd)
    verifyNoMoreInteractions(listener)
  }

  @Test
  fun loadAd_GivenNotANativeBid_NotifyListenerOnUiThreadForFailure() {
    expectListenerToBeCalledOnUiThread()
    givenNotANativeBidAvailable()

    nativeLoader.loadAd()

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
    verifyNoMoreInteractions(listener)
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
      on { map(nativeAssets) } doReturn nativeAd
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
        assertThat(runOnUiThreadExecutor.isRunningOnUiThread).isTrue()
        null
      }

      on { onAdReceived(any()) } doAnswer {
        assertThat(runOnUiThreadExecutor.isRunningOnUiThread).isTrue()
        null
      }
    }
  }

}