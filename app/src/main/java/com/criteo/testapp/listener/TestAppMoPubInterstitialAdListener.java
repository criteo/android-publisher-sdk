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
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class TestAppMoPubInterstitialAdListener implements InterstitialAdListener {

  private final String tag;
  private MoPubInterstitial mInterstitial;

  public TestAppMoPubInterstitialAdListener(String tag, MoPubInterstitial mInterstitial) {
    this.tag = tag;
    this.mInterstitial = mInterstitial;
  }

  @Override
  public void onInterstitialLoaded(MoPubInterstitial interstitial) {
    Log.d(tag, "Mopub ad loaded");
    mInterstitial.show();

  }

  @Override
  public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
    // Code to be executed when an ad request fails
    Log.d(tag, "Mopub ad failed:" + errorCode);
  }

  @Override
  public void onInterstitialShown(MoPubInterstitial interstitial) {
    Log.d(tag, "ad shown");
  }

  @Override
  public void onInterstitialClicked(MoPubInterstitial interstitial) {
    Log.d(tag, "Mopub ad clicked");

  }

  @Override
  public void onInterstitialDismissed(MoPubInterstitial interstitial) {
    Log.d(tag, "Mopub ad dismissed");

  }
}
