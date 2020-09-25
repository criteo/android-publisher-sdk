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
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;

public class TestAppInterstitialAdListener implements CriteoInterstitialAdListener {

  private final String tag;
  private final String prefix;
  private final Button btnShowInterstitial;

  public TestAppInterstitialAdListener(
      String tag, String prefix,
      Button btnShowInterstitial
  ) {
    this.tag = tag;
    this.prefix = prefix;
    this.btnShowInterstitial = btnShowInterstitial;
  }

  @UiThread
  @Override
  public void onAdReceived(@NonNull CriteoInterstitial interstitial) {
    btnShowInterstitial.setEnabled(true);
    Log.d(tag, prefix + "Interstitial ad called onAdReceived");
  }

  @Override
  public void onAdFailedToReceive(@NonNull CriteoErrorCode code) {
    Log.d(tag, prefix + " - Interstitial onAdFailedToReceive");
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - Interstitial onAdLeftApplication");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - Interstitial onAdClicked");
  }

  @Override
  public void onAdOpened() {
    Log.d(tag, prefix + " - Interstitial onAdOpened");
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - Interstitial onAdClosed");
  }

}
