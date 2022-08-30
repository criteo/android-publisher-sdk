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

package com.criteo.publisher.model.nativeads

import com.criteo.publisher.annotation.OpenForTesting
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.IOException
import java.net.URI
import java.net.URL

@OpenForTesting
@JsonClass(generateAdapter = true)
data class NativeAssets(
    /**
     * Always returns at least one product
     */
    @Json(name = "products")
    val nativeProducts: List<NativeProduct>,
    val advertiser: NativeAdvertiser,
    val privacy: NativePrivacy,
    @Json(name = "impressionPixels")
    val pixels: List<NativeImpressionPixel>
) {
  init {
    if (nativeProducts.isEmpty()) {
      throw IOException("Expect that native payload has, at least, one product.")
    }
    if (pixels.isEmpty()) {
      throw IOException("Expect that native payload has, at least, one impression pixel.")
    }
  }

  /**
   * Return the first product in the payload.
   * <p>
   * For the moment only one native product is handled by the SDK, so the {@link
   * #getNativeProducts()} is package private.
   *
   * @return first product in this native asset
   */
  val product: NativeProduct get() = nativeProducts.iterator().next()
  val advertiserDescription: String get() = advertiser.description
  val advertiserDomain: String get() = advertiser.domain
  val advertiserLogoUrl: URL get() = advertiser.logo.url
  val advertiserLogoClickUrl: URI get() = advertiser.logoClickUrl
  val privacyOptOutClickUrl: URI get() = privacy.clickUrl
  val privacyOptOutImageUrl: URL get() = privacy.imageUrl
  val privacyLongLegalText: String get() = privacy.legalText
  val impressionPixels: List<URL> get() = pixels.map { it.url }
}
