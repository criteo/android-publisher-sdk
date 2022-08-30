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

import com.criteo.publisher.Clock
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.util.URLUtil
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject
import java.io.ByteArrayInputStream

@OpenForTesting
@JsonClass(generateAdapter = true)
data class CdbResponseSlot(
    @Json(name = "impId") val impressionId: String? = null,
    @Json(name = "placementId") val placementId: String? = null,
    @Json(name = "zoneId") val zoneId: Int? = null,
    @Json(name = "cpm") val cpm: String = "0.0",
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "width") val width: Int = 0,
    @Json(name = "height") val height: Int = 0,
    @Json(name = "displayUrl") val displayUrl: String? = null,
    @Json(name = "native") val nativeAssets: NativeAssets? = null,
    @Json(name = "ttl") var ttlInSeconds: Int = 0,
    @Json(name = "isVideo") var isVideo: Boolean = false,
    @Json(name = "isRewarded") var isRewarded: Boolean = false,

    /**
     * The time of download in milliseconds for this bid response. This time represent a
     * client-side time given by a [com.criteo.publisher.Clock].
     */
    var timeOfDownload: Long = 0L
) {

  companion object {
    private const val SECOND_TO_MILLI = 1000

    @JvmStatic
    fun fromJson(json: JSONObject): CdbResponseSlot {
      // TODO remove this after CDB response parsing is totally migrated to Gson
      val jsonSerializer = DependencyProvider.getInstance().provideJsonSerializer()
      val jsonStr = json.toString()

      ByteArrayInputStream(jsonStr.toByteArray()).use { input ->
        return jsonSerializer.read(CdbResponseSlot::class.java, input)
      }
    }
  }

  val cpmAsNumber: Double? by lazy { cpm.toDoubleOrNull() }
  val isNative: Boolean by lazy { nativeAssets != null }

  fun isValid(): Boolean {
    val hasInvalidCpm = cpmAsNumber ?: -1.0 < 0.0
    val isNoBid = cpmAsNumber == 0.0 && ttlInSeconds == 0
    val isSilentBid = cpmAsNumber == 0.0 && ttlInSeconds > 0

    return when {
      hasInvalidCpm || isNoBid -> false
      isSilentBid -> true
      else -> isNative || URLUtil.isValidUrl(displayUrl)
    }
  }

  fun isExpired(clock: Clock): Boolean {
    val expiryTimeMillis: Long = ttlInSeconds * SECOND_TO_MILLI + timeOfDownload
    return expiryTimeMillis <= clock.currentTimeInMillis
  }
}
