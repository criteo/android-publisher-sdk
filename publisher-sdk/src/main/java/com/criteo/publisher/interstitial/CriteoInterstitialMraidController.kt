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

import androidx.annotation.MainThread
import com.criteo.publisher.advancednative.VisibilityTracker
import com.criteo.publisher.adview.CriteoMraidController
import com.criteo.publisher.adview.MraidActionResult
import com.criteo.publisher.adview.MraidInteractor
import com.criteo.publisher.adview.MraidMessageHandler
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.util.DeviceUtil

@OpenForTesting
internal class CriteoInterstitialMraidController(
    private val interstitialAdWebView: InterstitialAdWebView,
    private val runOnUiThreadExecutor: RunOnUiThreadExecutor,
    visibilityTracker: VisibilityTracker,
    mraidInteractor: MraidInteractor,
    mraidMessageHandler: MraidMessageHandler,
    deviceUtil: DeviceUtil
) : CriteoMraidController(
    interstitialAdWebView, visibilityTracker, mraidInteractor, mraidMessageHandler, deviceUtil
) {
  override fun getPlacementType(): MraidPlacementType = MraidPlacementType.INTERSTITIAL

  override fun doExpand(
      width: Double,
      height: Double,
      @MainThread onResult: (result: MraidActionResult) -> Unit
  ) {
    runOnUiThreadExecutor.execute {
      onResult(MraidActionResult.Error("Interstitial ad can't be expanded", "expand"))
    }
  }

  override fun doClose(@MainThread onResult: (result: MraidActionResult) -> Unit) {
    when (currentState) {
      MraidState.LOADING -> onResult(
          MraidActionResult.Error(
              "Can't close from loading state",
              CLOSE_ACTION
          )
      )
      MraidState.DEFAULT -> close(onResult)
      MraidState.EXPANDED -> onResult(MraidActionResult.Error("", CLOSE_ACTION))
      MraidState.HIDDEN -> onResult(
          MraidActionResult.Error(
              "Can't close from hidden state",
              CLOSE_ACTION
          )
      )
    }
  }

  private fun close(onResult: (result: MraidActionResult) -> Unit) {
    interstitialAdWebView.requestClose()
    onResult(MraidActionResult.Success)
  }

  private companion object {
    private const val CLOSE_ACTION = "close"
  }
}
