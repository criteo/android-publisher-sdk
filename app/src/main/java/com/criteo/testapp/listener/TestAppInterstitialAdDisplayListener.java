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

package com.criteo.testapp.listener;

import android.util.Log;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;

public class TestAppInterstitialAdDisplayListener implements CriteoInterstitialAdDisplayListener {

  private final String tag;
  private final String prefix;

  public TestAppInterstitialAdDisplayListener(String tag, String prefix) {
    this.tag = tag;
    this.prefix = prefix;
  }

  @Override
  public void onAdReadyToDisplay() {
    Log.d(tag, prefix + "Interstitial ad called onAdReadyToDisplay");
  }

  @Override
  public void onAdFailedToDisplay(CriteoErrorCode code) {
    Log.d(tag, prefix + "Interstitial ad called onAdFailedToDisplay");
  }
}
