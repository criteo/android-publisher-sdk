package com.criteo.publisher.model

import com.criteo.publisher.util.AdUnitType.*
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class CdbRequestSlotTest {

  private companion object {
    const val IMPRESSION_ID = "impId"
    const val PLACEMENT_ID = "placementId"
    const val SIZES = "sizes"
    const val IS_INTERSTITIAL = "interstitial"
    const val IS_NATIVE = "isNative"
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

  private fun JSONObject.assertPayloadMatch(
      expectedImpressionId: String,
      expectedPlacementId: String,
      expectedSizes: List<String>,
      expectedIsInterstitial: Boolean = false,
      expectedIsNative: Boolean = false
  ) {
    assertThat(keys()).containsExactlyInAnyOrder(
        IMPRESSION_ID,
        PLACEMENT_ID,
        SIZES,
        IS_INTERSTITIAL,
        IS_NATIVE
    )

    assertThat(this[IMPRESSION_ID]).isEqualTo(expectedImpressionId)
    assertThat(this[PLACEMENT_ID]).isEqualTo(expectedPlacementId)
    assertThat(this.readSizes()).isEqualTo(expectedSizes)
    assertThat(this[IS_NATIVE]).isEqualTo(expectedIsNative)
    assertThat(this[IS_INTERSTITIAL]).isEqualTo(expectedIsInterstitial)
  }

  private fun JSONObject.readSizes(): List<String> {
    val sizes: JSONArray = this[SIZES] as JSONArray

    return (0 until sizes.length())
        .map { sizes[it] as String }
        .toList()
  }

}