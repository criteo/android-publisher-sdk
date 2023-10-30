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

package com.criteo.publisher.adview

import android.content.res.Configuration
import android.webkit.WebViewClient
import androidx.annotation.MainThread

interface MraidController {

  val currentState: MraidState

  fun getPlacementType(): MraidPlacementType

  fun doExpand(
      width: Double,
      height: Double,
      @MainThread onResult: (result: MraidActionResult) -> Unit
  )

  fun doClose(@MainThread onResult: (result: MraidActionResult) -> Unit)

  @Suppress("LongParameterList")
  fun doResize(
      width: Double,
      height: Double,
      offsetX: Double,
      offsetY: Double,
      customClosePosition: MraidResizeCustomClosePosition,
      allowOffscreen: Boolean,
      @MainThread onResult: (result: MraidResizeActionResult) -> Unit
  )

  fun onWebViewClientSet(client: WebViewClient)

  fun onConfigurationChange(newConfig: Configuration?)

  /**
   * Notify when ad was closed by non-MRAID call (eg. by clicking on SDK provided button)
   */
  fun onClosed()

  /**
   * Brings back [AdWebViewClient] to default container if
   * it absent
   */
  fun resetToDefault()
}

sealed class MraidActionResult {
  object Success : MraidActionResult()
  data class Error(val message: String, val action: String) : MraidActionResult()
}

sealed class MraidResizeActionResult {
  data class Success(val x: Int, val y: Int, val width: Int, val height: Int) :
      MraidResizeActionResult()

  data class Error(val message: String, val action: String) : MraidResizeActionResult()
}
