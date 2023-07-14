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

import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.AdUnitType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@OpenForTesting
@JsonClass(generateAdapter = true)
data class CdbRequestSlot internal constructor(
    @Json(name = "impId")
    val impressionId: String,
    @Json(name = "placementId")
    val placementId: String,
    @Json(name = "isNative")
    val isNativeAd: Boolean?,
    @Json(name = "interstitial")
    val isInterstitial: Boolean?,
    @Json(name = "rewarded")
    val isRewarded: Boolean?,
    @Json(name = "sizes")
    val sizes: Collection<String>,
    @Json(name = "banner")
    val banner: Banner?
) {

  constructor(
      impressionId: String,
      placementId: String,
      adUnitType: AdUnitType,
      size: AdSize,
      apiFramework: List<ApiFramework>
  ) :
      this(
          impressionId,
          placementId,
          if (adUnitType == AdUnitType.CRITEO_CUSTOM_NATIVE) true else null,
          if (adUnitType == AdUnitType.CRITEO_INTERSTITIAL) true else null,
          if (adUnitType == AdUnitType.CRITEO_REWARDED) true else null,
          listOf(size.formattedSize),
          apiFramework.takeIf { it.isNotEmpty() }?.run { Banner(api = this.map { it.code }) }
      )
}

@JsonClass(generateAdapter = true)
data class Banner internal constructor(
    @Json(name = "api")
    val api: List<Int>
)
