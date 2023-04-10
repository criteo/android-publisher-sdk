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

import android.webkit.WebView
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.logging.LoggerFactory

@OpenForTesting
internal class MraidInteractor(private val webView: WebView) {

  private val logger = LoggerFactory.getLogger(MraidInteractor::class.java)

  fun notifyReady(placementType: MraidPlacementType) {
    "notifyReady"(placementType.value)
  }

  fun notifyError(message: String, action: String? = null) {
    "notifyError"(message, action)
  }

  fun setIsViewable(isViewable: Boolean) {
    "setIsViewable"(isViewable)
  }

  fun notifyExpanded() {
    "notifyExpanded"()
  }

  fun notifyClosed() {
    "notifyClosed"()
  }

  /**
   * [width] in dp
   * [height] in dp
   * [pixelMultiplier] - value co calculate width or height in pixels
   */
  fun setMaxSize(width: Int, height: Int, pixelMultiplier: Double) {
    "setMaxSize"(width, height, pixelMultiplier)
  }

  private operator fun String.invoke(vararg params: Any? = emptyArray()) {
    callOnMraidObject("$this(${asJsArgs(*params)})")
  }

  private fun callOnMraidObject(jsCode: String) {
    val jsToEvaluate = "window.mraid.$jsCode"
    logger.debug("Calling mraid object with js: $jsToEvaluate")
    webView.evaluateJavascript(jsToEvaluate, null)
  }

  private fun asJsArgs(vararg params: Any?): String {
    return params.joinToString(separator = ", ") {
      when (it) {
        null -> "undefined"
        is String -> "\"$it\""
        is Boolean -> it.toString()
        is Int -> it.toString()
        is Double -> it.toString()
        else -> throw UnsupportedOperationException(
            "${it.javaClass.name} conversion is not supported, please update code if you need this conversion"
        )
      }
    }
  }
}
