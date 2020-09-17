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

package com.criteo.publisher.util;

import static com.criteo.publisher.CriteoListenerCode.CLICK;
import static com.criteo.publisher.CriteoListenerCode.CLOSE;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;

public class CriteoResultReceiver extends ResultReceiver {

  public static final String INTERSTITIAL_ACTION = "Action";
  public static final int RESULT_CODE_SUCCESSFUL = 100;
  public static final int ACTION_CLOSED = 201;
  public static final int ACTION_LEFT_CLICKED = 202;

  @NonNull
  private final InterstitialListenerNotifier listenerNotifier;

  /**
   * Create a new ResultReceive to receive results.  Your {@link #onReceiveResult} method will be
   * called from the thread running
   * <var>handler</var> if given, or from an arbitrary thread if null.
   */
  public CriteoResultReceiver(
      @NonNull Handler handler,
      @NonNull InterstitialListenerNotifier listenerNotifier
  ) {
    super(handler);
    this.listenerNotifier = listenerNotifier;
  }

  @Override
  protected void onReceiveResult(int resultCode, Bundle resultData) {
    if (resultCode == RESULT_CODE_SUCCESSFUL) {
      int action = resultData.getInt(INTERSTITIAL_ACTION);

      switch (action) {
        case ACTION_CLOSED:
          listenerNotifier.notifyFor(CLOSE);
          break;
        case ACTION_LEFT_CLICKED:
          listenerNotifier.notifyFor(CLICK);
          break;
      }
    }
  }
}
