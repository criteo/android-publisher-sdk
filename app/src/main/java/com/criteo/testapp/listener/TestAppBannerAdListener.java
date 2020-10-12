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
import android.view.ViewGroup;
import android.view.View;
import androidx.annotation.NonNull;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;

public class TestAppBannerAdListener implements CriteoBannerAdListener {

  private final String tag;
  private final String prefix;

  public TestAppBannerAdListener(String tag, String prefix) {
    this.tag = tag;
    this.prefix = prefix;
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - Banner onAdLeftApplication");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - Banner onAdClicked");
  }

  @Override
  public void onAdOpened() {
    Log.d(tag, prefix + " - Banner onAdOpened");
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - Banner onAdClosed");
  }

  @Override
  public void onAdFailedToReceive(@NonNull CriteoErrorCode code) {
    Log.d(tag, prefix + " - Banner onAdFailedToReceive, reason : " + code.toString());
  }

  @Override
  public void onAdReceived(@NonNull CriteoBannerView view) {
    Log.d(tag, prefix + " - Banner onAdReceived");
  }

}
