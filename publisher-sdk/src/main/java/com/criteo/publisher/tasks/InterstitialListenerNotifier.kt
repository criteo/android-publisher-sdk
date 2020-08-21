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

package com.criteo.publisher.tasks

import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NETWORK_ERROR
import com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL
import com.criteo.publisher.CriteoInterstitialAdListener
import com.criteo.publisher.CriteoListenerCode
import com.criteo.publisher.CriteoListenerCode.CLICK
import com.criteo.publisher.CriteoListenerCode.CLOSE
import com.criteo.publisher.CriteoListenerCode.INVALID
import com.criteo.publisher.CriteoListenerCode.INVALID_CREATIVE
import com.criteo.publisher.CriteoListenerCode.OPEN
import com.criteo.publisher.CriteoListenerCode.VALID
import com.criteo.publisher.SafeRunnable
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import java.lang.ref.Reference
import java.lang.ref.WeakReference

@OpenForTesting
class InterstitialListenerNotifier @VisibleForTesting internal constructor(
    private val listenerRef: Reference<CriteoInterstitialAdListener>,
    private val runOnUiThreadExecutor: RunOnUiThreadExecutor
) {

  constructor(
      listener: CriteoInterstitialAdListener?,
      runOnUiThreadExecutor: RunOnUiThreadExecutor
  ) : this(WeakReference<CriteoInterstitialAdListener>(listener), runOnUiThreadExecutor)

  fun notifyFor(code: CriteoListenerCode) {
    runOnUiThreadExecutor.executeAsync(object : SafeRunnable() {
      override fun runSafely() {
        listenerRef.get()?.notifyFor(code)
      }
    })
  }

  @UiThread
  private fun CriteoInterstitialAdListener.notifyFor(code: CriteoListenerCode) {
    when (code) {
      VALID -> onAdReceived()
      INVALID -> onAdFailedToReceive(ERROR_CODE_NO_FILL)
      INVALID_CREATIVE -> onAdFailedToReceive(ERROR_CODE_NETWORK_ERROR)
      OPEN -> onAdOpened()
      CLOSE -> onAdClosed()
      CLICK -> {
        onAdClicked()
        onAdLeftApplication()
      }
    }
  }
}
