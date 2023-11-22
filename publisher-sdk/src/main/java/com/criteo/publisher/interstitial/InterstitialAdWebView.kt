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

package com.criteo.publisher.interstitial

import android.content.Context
import android.util.AttributeSet
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.adview.AdWebView
import com.criteo.publisher.adview.MraidController
import com.criteo.publisher.adview.MraidOrientation
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.annotation.OpenForTesting

@OpenForTesting
internal class InterstitialAdWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AdWebView(context, attrs) {

  private var onCloseRequestedListener: (() -> Unit)? = null
  private var onOrientationRequestedListener:
      ((allowOrientationChange: Boolean, forceOrientation: MraidOrientation) -> Unit)? = null

  override fun provideMraidController(): MraidController {
    return DependencyProvider.getInstance()
        .provideMraidController(MraidPlacementType.INTERSTITIAL, this)
  }

  fun setOnCloseRequestedListener(onCloseRequestedListener: () -> Unit) {
    this.onCloseRequestedListener = onCloseRequestedListener
  }

  fun setOnOrientationRequestedListener(
      onOrientationRequestedListener:
      (allowOrientationChange: Boolean, forceOrientation: MraidOrientation) -> Unit
  ) {
    this.onOrientationRequestedListener = onOrientationRequestedListener
  }

  fun requestClose() {
    onCloseRequestedListener?.invoke()
  }

  fun requestOrientationChange(
      allowOrientationChange: Boolean,
      forceOrientation: MraidOrientation
  ) {
    onOrientationRequestedListener?.invoke(allowOrientationChange, forceOrientation)
  }

  fun onClosed() {
    mraidController.onClosed()
  }
}
