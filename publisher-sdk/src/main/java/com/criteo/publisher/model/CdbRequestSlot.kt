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
import com.google.gson.annotations.SerializedName

@OpenForTesting
data class CdbRequestSlot private constructor(
    @SerializedName("impId")
    val impressionId: String,
    @SerializedName("placementId")
    val placementId: String,
    @SerializedName("isNative")
    val isNativeAd: Boolean?,
    @SerializedName("interstitial")
    val isInterstitial: Boolean?,
    @SerializedName("rewarded")
    val isRewarded: Boolean?,
    @SerializedName("sizes")
    val sizes: Collection<String>
) {

  constructor(impressionId: String, placementId: String, adUnitType: AdUnitType, size: AdSize) :
      this(
          impressionId,
          placementId,
          if (adUnitType == AdUnitType.CRITEO_CUSTOM_NATIVE) true else null,
          if (adUnitType == AdUnitType.CRITEO_INTERSTITIAL) true else null,
          if (adUnitType == AdUnitType.CRITEO_REWARDED) true else null,
          listOf(size.formattedSize)
      )
}
