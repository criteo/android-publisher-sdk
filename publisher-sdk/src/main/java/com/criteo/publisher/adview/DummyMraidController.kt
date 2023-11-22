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

internal class DummyMraidController : MraidController {
  override val currentState: MraidState = MraidState.LOADING

  override fun getPlacementType(): MraidPlacementType {
    return MraidPlacementType.INLINE
  }

  override fun doExpand(
      width: Double,
      height: Double,
      onResult: (result: MraidActionResult) -> Unit
  ) {
    // no-op
  }

  override fun doClose(onResult: (result: MraidActionResult) -> Unit) {
    // no-op
  }

  override fun doResize(
      width: Double,
      height: Double,
      offsetX: Double,
      offsetY: Double,
      customClosePosition: MraidResizeCustomClosePosition,
      allowOffscreen: Boolean,
      onResult: (result: MraidResizeActionResult) -> Unit
  ) {
    // no-op
  }

  override fun doSetOrientationProperties(
      allowOrientationChange: Boolean,
      forceOrientation: MraidOrientation,
      onResult: (result: MraidActionResult) -> Unit
  ) {
    // no-op
  }

  override fun onWebViewClientSet(client: WebViewClient) {
    // no-op
  }

  override fun onConfigurationChange(newConfig: Configuration?) {
    // no-op
  }

  override fun onClosed() {
    // no-op
  }

  override fun resetToDefault() {
    // no-op
  }
}
