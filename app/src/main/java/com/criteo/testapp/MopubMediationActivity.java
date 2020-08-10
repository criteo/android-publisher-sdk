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

import static com.criteo.testapp.MopubActivity.initializeMoPubSdk;
import static com.criteo.testapp.PubSdkDemoApplication.MOPUB_BANNER_ADUNIT_ID;
import static com.criteo.testapp.PubSdkDemoApplication.MOPUB_INTERSTITIAL_ADUNIT_ID;
import static com.criteo.testapp.PubSdkDemoApplication.MOPUB_NATIVE_ADUNIT_ID;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.mediation.mopub.advancednative.CriteoNativeEventRenderer;
import com.criteo.publisher.integration.Integration;
import com.criteo.testapp.integration.MockedIntegrationRegistry;
import com.criteo.testapp.listener.TestAppMoPubInterstitialAdListener;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubNative.MoPubNativeNetworkListener;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeAd.MoPubNativeEventListener;
import com.mopub.nativeads.NativeErrorCode;

public class MopubMediationActivity extends AppCompatActivity {

  private static final String TAG = MopubMediationActivity.class.getSimpleName();

  private MoPubView publisherAdView;
  private LinearLayout linearLayout;

  MoPubNative moPubNative;
  MoPubInterstitial mInterstitial;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mopub_mediation);
    MockedIntegrationRegistry.force(Integration.MOPUB_MEDIATION);

    initializeMoPubSdk(this);

    initMoPubNative();
    mInterstitial = new MoPubInterstitial(this, MOPUB_INTERSTITIAL_ADUNIT_ID);

    linearLayout = findViewById(R.id.AdLayout);
    findViewById(R.id.buttonMopubMediationBanner).setOnClickListener((View v) -> onBannerClick());
    findViewById(R.id.buttonMopubMediationInterstitial)
        .setOnClickListener((View v) -> onInterstitialClick());
    findViewById(R.id.buttonMopubMediationNative).setOnClickListener((View v) -> onNativeClick());
  }

  private void initMoPubNative() {
    moPubNative = new MoPubNative(this, MOPUB_NATIVE_ADUNIT_ID, new MoPubNativeNetworkListener() {
      @Override
      public void onNativeLoad(NativeAd nativeAd) {
        Log.d(TAG, "Native onNativeLoad");
        nativeAd.setMoPubNativeEventListener(new NativeEventListener());

        AdapterHelper adapterHelper = new AdapterHelper(MopubMediationActivity.this, 0, 2);
        View adView = adapterHelper.getAdView(linearLayout, null, nativeAd);
        linearLayout.removeAllViews();
        linearLayout.addView(adView);
      }

      @Override
      public void onNativeFail(NativeErrorCode errorCode) {
        Log.d(TAG, "Native onNativeFail, reason: " + errorCode);
      }
    });

    moPubNative.registerAdRenderer(new CriteoNativeEventRenderer(new TestAppNativeRenderer()));
  }

  private void onBannerClick() {
    linearLayout.setBackgroundColor(Color.RED);
    linearLayout.removeAllViews();
    linearLayout.setVisibility(View.VISIBLE);
    publisherAdView = new MoPubView(this);
    publisherAdView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID);
    publisherAdView.loadAd();
    linearLayout.addView(publisherAdView);
  }

  private void onInterstitialClick() {
    mInterstitial.setInterstitialAdListener(
        new TestAppMoPubInterstitialAdListener(TAG, mInterstitial)
    );

    mInterstitial.load();
  }

  private void onNativeClick() {
    moPubNative.makeRequest();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    moPubNative.destroy();
    mInterstitial.destroy();
    if (publisherAdView != null) {
      publisherAdView.destroy();
    }
  }

  private static class NativeEventListener implements MoPubNativeEventListener {
    @Override
    public void onImpression(View view) {
      Log.d(TAG, "Native onImpression");
    }

    @Override
    public void onClick(View view) {
      Log.d(TAG, "Native onClick");
    }
  }
}
