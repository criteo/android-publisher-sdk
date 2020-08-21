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

package com.criteo.publisher.tasks;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NETWORK_ERROR;
import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.CriteoListenerCode;

public class CriteoInterstitialListenerCallTask implements Runnable {

  private static final String TAG = "Criteo.ILCT";

  @Nullable
  private final CriteoInterstitialAdListener criteoInterstitialAdListener;

  @NonNull
  private final CriteoListenerCode code;

  public CriteoInterstitialListenerCallTask(
      @Nullable CriteoInterstitialAdListener listener,
      @NonNull CriteoListenerCode code) {
    this.criteoInterstitialAdListener = listener;
    this.code = code;
  }

  @Override
  public void run() {
    try {
      doRun();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal ILCT PostExec error.", tr);
    }
  }

  private void doRun() {
    if (criteoInterstitialAdListener == null) {
      return;
    }

    switch (code) {
      case VALID:
        criteoInterstitialAdListener.onAdReceived();
        break;
      case INVALID:
        criteoInterstitialAdListener.onAdFailedToReceive(ERROR_CODE_NO_FILL);
        break;
      case INVALID_CREATIVE:
        criteoInterstitialAdListener.onAdFailedToReceive(ERROR_CODE_NETWORK_ERROR);
        break;
    }
  }
}
