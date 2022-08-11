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

package com.criteo.testapp;

import static com.criteo.testapp.PubSdkDemoApplication.CONTEXT_DATA;
import static com.criteo.testapp.PubSdkDemoApplication.INTERSTITIAL;
import static com.criteo.testapp.PubSdkDemoApplication.INTERSTITIAL_IBV_DEMO;
import static com.criteo.testapp.PubSdkDemoApplication.NATIVE;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.testapp.integration.MockedIntegrationRegistry;
import com.criteo.testapp.listener.TestAppBannerAdListener;
import com.criteo.testapp.listener.TestAppInterstitialAdListener;
import com.criteo.testapp.listener.TestAppNativeAdListener;


public class StandaloneActivity extends BaseActivity {

  private static final String TAG = StandaloneActivity.class.getSimpleName();

  private CriteoBannerView criteoBannerView;
  private CriteoNativeLoader nativeLoader;
  private FrameLayout nativeAdContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stand_alone);
    MockedIntegrationRegistry.force(Integration.STANDALONE);

    criteoBannerView = findViewById(R.id.criteoBannerView);
    nativeAdContainer = findViewById(R.id.nativeAdContainer);

    criteoBannerView.setCriteoBannerAdListener(new TestAppBannerAdListener(
        TAG, "Standalone"));

    nativeLoader = new CriteoNativeLoader(
        NATIVE,
        new TestAppNativeAdListener(TAG, NATIVE.getAdUnitId(), nativeAdContainer),
        new TestAppNativeRenderer()
    );

    findViewById(R.id.buttonStandAloneBanner).setOnClickListener(v -> loadBannerAd());
    findViewById(R.id.buttonStandAloneNative).setOnClickListener(v -> loadNative());
    findViewById(R.id.buttonStandAloneInterstitial).setOnClickListener(v -> loadInterstitial(INTERSTITIAL));
    findViewById(R.id.buttonStandAloneInterstitialIbv).setOnClickListener(v -> loadInterstitial(INTERSTITIAL_IBV_DEMO));
  }

  private void loadBannerAd() {
    Log.d(TAG, "Banner Requested");
    criteoBannerView.loadAd(CONTEXT_DATA);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (criteoBannerView != null) {
      criteoBannerView.destroy();
    }
  }

  private void loadNative() {
    nativeLoader.loadAd(CONTEXT_DATA);
  }

  private void loadInterstitial(InterstitialAdUnit adUnit) {
    String prefix = "Standalone " + adUnit.getAdUnitId();

    CriteoInterstitial criteoInterstitial = new CriteoInterstitial(adUnit);
    criteoInterstitial.setCriteoInterstitialAdListener(new TestAppInterstitialAdListener(TAG, prefix));

    Log.d(TAG, prefix + "Interstitial Requested");
    criteoInterstitial.loadAd(CONTEXT_DATA);
  }

}
