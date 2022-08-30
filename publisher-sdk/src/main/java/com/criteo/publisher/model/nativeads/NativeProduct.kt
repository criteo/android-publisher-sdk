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
import com.squareup.moshi.JsonClass
import java.net.URI
import java.net.URL

@OpenForTesting
@JsonClass(generateAdapter = true)
data class NativeProduct(
    val title: String,
    val description: String,
    val price: String,
    /**
     * This is an {@link URI} and not an {@link URL}, because deeplink are acceptable.
     */
    val clickUrl: URI,
    val callToAction: String,
    val image: NativeImage
) {
  val imageUrl: URL get() = image.url
}
