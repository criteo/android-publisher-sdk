package com.criteo.publisher.headerbidding

import com.criteo.publisher.BidManager
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.InterstitialAdUnit
import com.criteo.publisher.model.Slot
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class OtherAdServersHeaderBiddingTest {

  companion object {
    const val CRT_CPM = "crt_cpm"
    const val CRT_DISPLAY_URL = "crt_displayUrl"
    const val CRT_SIZE = "crt_size"
  }

  @Mock
  private lateinit var bidManager: BidManager

  private lateinit var headerBidding: OtherAdServersHeaderBidding

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    headerBidding = OtherAdServersHeaderBidding(
        bidManager
    )
  }

  @Test
  fun canHandle_GivenSimpleObject_ReturnFalse() {
    val handling = headerBidding.canHandle(mock())

    assertThat(handling).isFalse()
  }

  @Test
  fun canHandle_GivenMapBuilder_ReturnTrue() {
    val map = mapOf<Any, Any>()

    val handling = headerBidding.canHandle(map)

    assertThat(handling).isTrue()
  }

  @Test
  fun enrichBid_GivenNotHandledObject_DoNothing() {
    headerBidding.enrichBid(mock(), mock())

    verifyZeroInteractions(bidManager)
  }

  @Test
  fun enrichBid_GivenMapAndNoBidAvailable_DoNotEnrich() {
    val adUnit = BannerAdUnit("adUnit", AdSize(1, 2))

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn null
    }

    val map = mutableMapOf<Any, Any>()

    headerBidding.enrichBid(map, adUnit)

    assertThat(map).isEmpty()
  }

  @Test
  fun enrichBid_GivenMapAndBannerBidAvailable_EnrichMap() {
    val adUnit = BannerAdUnit("adUnit", AdSize(42, 1337))

    val slot = mock<Slot>() {
      on { isNative } doReturn false
      on { cpm } doReturn "0.10"
      on { displayUrl } doReturn "http://display.url"
      on { width } doReturn 42
      on { height } doReturn 1337
    }

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn slot
    }

    val map = mutableMapOf<Any, Any>()
    map["garbage"] = "should stay"

    headerBidding.enrichBid(map, adUnit)

    assertThat(map).isEqualTo(mapOf<Any, Any>(
        "garbage" to "should stay",
        CRT_CPM to "0.10",
        CRT_DISPLAY_URL to "http://display.url",
        CRT_SIZE to "42x1337"
    ))
  }

  @Test
  fun enrichBid_GivenMapAndInterstitialBidAvailable_EnrichMap() {
    val adUnit = InterstitialAdUnit("adUnit")

    val slot = mock<Slot>() {
      on { isNative } doReturn false
      on { cpm } doReturn "0.10"
      on { displayUrl } doReturn "http://display.url"
    }

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn slot
    }

    val map = mutableMapOf<Any, Any>()
    map["garbage"] = "should stay"

    headerBidding.enrichBid(map, adUnit)

    assertThat(map).isEqualTo(mapOf<Any, Any>(
        "garbage" to "should stay",
        CRT_CPM to "0.10",
        CRT_DISPLAY_URL to "http://display.url"
    ))
  }

}