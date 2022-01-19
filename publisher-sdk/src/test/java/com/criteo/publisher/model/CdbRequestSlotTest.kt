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

package com.criteo.publisher.model

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE
import com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL
import com.criteo.publisher.util.AdUnitType.CRITEO_REWARDED
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class CdbRequestSlotTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var serializer: JsonSerializer

  private companion object {
    const val IMPRESSION_ID = "impId"
    const val PLACEMENT_ID = "placementId"
    const val SIZES = "sizes"
    const val IS_INTERSTITIAL = "interstitial"
    const val IS_NATIVE = "isNative"
    const val IS_REWARDED = "rewarded"
  }

  @Test
  fun toJson_GivenBannerAdUnit_ReturnJsonRepresentation() {
    val slot = CdbRequestSlot.create(
        "myImpBanner",
        "myBanner",
        CRITEO_BANNER,
        AdSize(42, 1337)
    )

    val json = slot.toJson()

    json.assertPayloadMatch(
        "myImpBanner",
        "myBanner",
        listOf("42x1337")
    )
  }

  @Test
  fun toJson_GivenInterstitialAdUnit_ReturnJsonRepresentation() {
    val slot = CdbRequestSlot.create(
        "myImpInterstitial",
        "myInterstitial",
        CRITEO_INTERSTITIAL,
        AdSize(1337, 42)
    )

    val json = slot.toJson()

    json.assertPayloadMatch(
        "myImpInterstitial",
        "myInterstitial",
        listOf("1337x42"),
        expectedIsInterstitial = true
    )
  }

  @Test
  fun toJson_GivenNativeAdUnit_ReturnJsonRepresentation() {
    val slot = CdbRequestSlot.create(
        "myImpNative",
        "myNative",
        CRITEO_CUSTOM_NATIVE,
        AdSize(2, 2)
    )

    val json = slot.toJson()

    json.assertPayloadMatch(
        "myImpNative",
        "myNative",
        listOf("2x2"),
        expectedIsNative = true
    )
  }

  @Test
  fun toJson_GivenRewardedAdUnit_ReturnJsonRepresentation() {
    val slot = CdbRequestSlot.create(
        "myImpRewarded",
        "myRewarded",
        CRITEO_REWARDED,
        AdSize(1337, 42)
    )

    val json = slot.toJson()

    json.assertPayloadMatch(
        "myImpRewarded",
        "myRewarded",
        listOf("1337x42"),
        expectedIsRewarded = true
    )
  }

  @Suppress("LongParameterList")
  private fun JSONObject.assertPayloadMatch(
      expectedImpressionId: String,
      expectedPlacementId: String,
      expectedSizes: List<String>,
      expectedIsInterstitial: Boolean = false,
      expectedIsNative: Boolean = false,
      expectedIsRewarded: Boolean = false
  ) {
    val expectedKeys = mutableListOf(
        IMPRESSION_ID,
        PLACEMENT_ID,
        SIZES
    )

    if (expectedIsInterstitial) {
      expectedKeys.add(IS_INTERSTITIAL)
    }
    if (expectedIsNative) {
      expectedKeys.add(IS_NATIVE)
    }
    if (expectedIsRewarded) {
      expectedKeys.add(IS_REWARDED)
    }

    assertThat(keys()).toIterable().containsExactlyInAnyOrderElementsOf(expectedKeys)

    assertThat(this[IMPRESSION_ID]).isEqualTo(expectedImpressionId)
    assertThat(this[PLACEMENT_ID]).isEqualTo(expectedPlacementId)
    assertThat(this.readSizes()).isEqualTo(expectedSizes)

    if (expectedIsNative) {
      assertThat(this[IS_NATIVE]).isEqualTo(true)
    }

    if (expectedIsInterstitial) {
      assertThat(this[IS_INTERSTITIAL]).isEqualTo(true)
    }

    if (expectedIsRewarded) {
      assertThat(this[IS_REWARDED]).isEqualTo(true)
    }
  }

  private fun JSONObject.readSizes(): List<String> {
    val sizes: JSONArray = this[SIZES] as JSONArray

    return (0 until sizes.length())
        .map { sizes[it] as String }
        .toList()
  }

  private fun CdbRequestSlot.toJson(): JSONObject = JSONObject(serializer.writeIntoString(this))
}
