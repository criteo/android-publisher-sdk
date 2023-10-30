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
@Suppress("TooManyFunctions")
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

  fun notifyResized() {
    "notifyResized"()
  }

  fun notifyClosed() {
    "notifyClosed"()
  }

  /**
   * Report max size available to render ad
   * [width] in dp
   * [height] in dp
   * [pixelMultiplier] - value to calculate width or height in pixels
   */
  fun setMaxSize(width: Int, height: Int, pixelMultiplier: Double) {
    "setMaxSize"(width, height, pixelMultiplier)
  }

  /**
   * Reports screen size of device in dp
   */
  fun setScreenSize(width: Int, height: Int) {
    "setScreenSize"(width, height)
  }

  fun setSupports(sms: Boolean, tel: Boolean) {
    "setSupports"(mapOf(
        "sms" to sms,
        "tel" to tel
    ))
  }

  fun setCurrentPosition(x: Int, y: Int, width: Int, height: Int) {
    "setCurrentPosition"(x, y, width, height)
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
        is Map<*, *> -> "{${it.map { it.key.toString() + ": " + asJsArgs(it.value)}.joinToString(", ")}}"
        else -> throw UnsupportedOperationException(
            "${it.javaClass.name} conversion is not supported, please update code if you need this conversion"
        )
      }
    }
  }
}
