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
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdListener;

public class TestAppNativeAdListener extends CriteoNativeAdListener {

  private final String tag;
  private final String prefix;
  private final ViewGroup adLayout;

  public TestAppNativeAdListener(String tag, String prefix, @Nullable ViewGroup adLayout) {
    this.tag = tag;
    this.prefix = prefix;
    this.adLayout = adLayout;
  }

  @Override
  public void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
    Log.d(tag, prefix + " - Native onAdReceived");
    View view = nativeAd.createNativeRenderedView(adLayout.getContext(), adLayout);
    adLayout.removeAllViews();
    adLayout.addView(view);
  }

  @Override
  public void onAdFailedToReceive(@NonNull CriteoErrorCode code) {
    Log.d(tag, prefix + " - Native onAdFailedToReceive, reason : " + code.toString());
  }

  @Override
  public void onAdImpression() {
    Log.d(tag, prefix + " - Native onAdImpression");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - Native onAdClicked");
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - Native onAdLeftApplication");
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - Native onAdClosed");
  }

}
