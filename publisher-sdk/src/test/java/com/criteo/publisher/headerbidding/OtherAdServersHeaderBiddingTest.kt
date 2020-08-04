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

package com.criteo.publisher.headerbidding

import com.criteo.publisher.integration.Integration
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.model.InterstitialAdUnit
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OtherAdServersHeaderBiddingTest {

  companion object {
    const val CRT_CPM = "crt_cpm"
    const val CRT_DISPLAY_URL = "crt_displayUrl"
    const val CRT_SIZE = "crt_size"
  }

  private val headerBidding = OtherAdServersHeaderBidding()

  @Test
  fun getIntegration_ReturnCustomAppBidding() {
    val integration = headerBidding.integration

    assertThat(integration).isEqualTo(Integration.CUSTOM_APP_BIDDING)
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
  fun cleanPreviousBid_GivenNotHandledObject_DoNothing() {
    val builder = mock<Any>()

    headerBidding.cleanPreviousBid(builder)

    verifyZeroInteractions(builder)
  }

  @Test
  fun cleanPreviousBid_GivenMapWithoutCriteoInfo_DoNothing() {
    val map = mutableMapOf<Any, Any>("garbage" to "should stay")

    headerBidding.cleanPreviousBid(map)

    assertThat(map).hasSize(1).containsEntry("garbage", "should stay")
  }

  @Test
  fun cleanPreviousBid_GivenMapWithCriteoInfo_RemovingOnlyCriteoInfo() {
    val map = mutableMapOf<Any, Any>(
        "garbage" to "should stay",
        CRT_CPM to "0.10",
        CRT_DISPLAY_URL to "http://display.url"
    )

    headerBidding.cleanPreviousBid(map)

    assertThat(map).hasSize(1).containsEntry("garbage", "should stay")
  }

  @Test
  fun enrichBid_GivenNotHandledObject_DoNothing() {
    val builder = mock<Any>()

    headerBidding.enrichBid(builder, mock(), mock())

    verifyZeroInteractions(builder)
  }

  @Test
  fun enrichBid_GivenMapAndBannerBidAvailable_EnrichMap() {
    val adUnit = BannerAdUnit("adUnit", AdSize(42, 1337))

    val slot = mock<CdbResponseSlot>() {
      on { isNative } doReturn false
      on { cpm } doReturn "0.10"
      on { displayUrl } doReturn "http://display.url"
      on { width } doReturn 42
      on { height } doReturn 1337
    }

    val map = mutableMapOf<Any, Any>()
    map["garbage"] = "should stay"

    headerBidding.enrichBid(map, adUnit, slot)

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

    val slot = mock<CdbResponseSlot>() {
      on { isNative } doReturn false
      on { cpm } doReturn "0.10"
      on { displayUrl } doReturn "http://display.url"
    }

    val map = mutableMapOf<Any, Any>()
    map["garbage"] = "should stay"

    headerBidding.enrichBid(map, adUnit, slot)

    assertThat(map).isEqualTo(mapOf<Any, Any>(
        "garbage" to "should stay",
        CRT_CPM to "0.10",
        CRT_DISPLAY_URL to "http://display.url"
    ))
  }

}