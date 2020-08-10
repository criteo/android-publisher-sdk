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

import static com.mopub.common.logging.MoPubLog.LogLevel.DEBUG;
import static com.mopub.common.logging.MoPubLog.LogLevel.INFO;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.testapp.integration.MockedIntegrationRegistry;
import com.criteo.testapp.listener.TestAppMoPubInterstitialAdListener;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

public class MopubActivity extends AppCompatActivity {

  private static final String TAG = MopubActivity.class.getSimpleName();
  private static final String SDK_BUILD_ID = "b195f8dd8ded45fe847ad89ed1d016da";
  public static final String MOPUB_BANNER_ADUNIT_ID_HB = "d2f3ed80e5da4ae1acde0971eac30fa4";
  public static final String MOPUB_INTERSTITIAL_ADUNIT_ID_HB = "83a2996696284da881edaf1a480e5d7c";

  private static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
      MOPUB_INTERSTITIAL_ADUNIT_ID_HB);

  private static final BannerAdUnit BANNER = new BannerAdUnit(
      MOPUB_BANNER_ADUNIT_ID_HB,
      new AdSize(320, 50));

  private MoPubView publisherAdView;
  private LinearLayout linearLayout;
  private Criteo criteo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mopub);
    MockedIntegrationRegistry.force(Integration.MOPUB_APP_BIDDING);
    criteo = Criteo.getInstance();

    initializeMoPubSdk(this);

    linearLayout = findViewById(R.id.adViewHolder);
    findViewById(R.id.buttonBanner).setOnClickListener((View v) -> onBannerClick());
    findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> onInterstitialClick());
  }

  public static void initializeMoPubSdk(Context context) {
    final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder(SDK_BUILD_ID);
    if (BuildConfig.DEBUG) {
      configBuilder.withLogLevel(DEBUG);
    } else {
      configBuilder.withLogLevel(INFO);
    }
    MoPub.initializeSdk(context, configBuilder.build(), new SdkInitializationListener() {
      @Override
      public void onInitializationFinished() {
        Log.d(TAG, "Mopub initialization completed");
      }
    });
  }

  private void onBannerClick() {
    linearLayout.setBackgroundColor(Color.RED);
    linearLayout.removeAllViews();
    linearLayout.setVisibility(View.VISIBLE);

    publisherAdView = new MoPubView(this);
    criteo.setBidsForAdUnit(publisherAdView, BANNER);
    publisherAdView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_HB);
    publisherAdView.loadAd();

    linearLayout.addView(publisherAdView);
  }

  private void onInterstitialClick() {
    MoPubInterstitial mInterstitial = new MoPubInterstitial(this, MOPUB_INTERSTITIAL_ADUNIT_ID_HB);
    criteo.setBidsForAdUnit(mInterstitial, INTERSTITIAL);
    mInterstitial.setInterstitialAdListener(
        new TestAppMoPubInterstitialAdListener(TAG, mInterstitial));
    mInterstitial.load();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (publisherAdView != null) {
      publisherAdView.destroy();
    }
  }

}
