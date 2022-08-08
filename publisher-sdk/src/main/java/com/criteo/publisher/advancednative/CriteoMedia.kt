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

package com.criteo.publisher.advancednative

import androidx.annotation.Keep
import com.criteo.publisher.annotation.Internal
import com.criteo.publisher.annotation.Internal.ADMOB_ADAPTER
import com.criteo.publisher.annotation.OpenForTesting
import com.google.gson.annotations.SerializedName
import java.net.URL

@OpenForTesting
@Keep
data class CriteoMedia private constructor(
    @SerializedName("imageUrl")
    @Internal(ADMOB_ADAPTER)
    val imageUrl: URL
) {
  companion object {
    @JvmStatic
    fun create(imageUrl: URL): CriteoMedia {
      return CriteoMedia(imageUrl)
    }
  }
}
