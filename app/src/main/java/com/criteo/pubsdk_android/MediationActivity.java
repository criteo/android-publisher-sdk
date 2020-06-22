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

package com.criteo.pubsdk_android;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


// TODO: Move this class to the test app repo
public class MediationActivity extends AppCompatActivity {

  private static final String TAG = MediationActivity.class.getSimpleName();

  private static final String APP_ID = "ca-app-pub-8459323526901202~2792049297";
  private static final String BANNER_ADUNIT_ID = "ca-app-pub-8459323526901202/2832836926";
  private static final String INTERSTITIAL_ADUNIT_ID = "ca-app-pub-8459323526901202/6462812944";
  private static final String TESTDEVICE_ID = "3D7BB698B9A4CAA9F0D8878A5A5E7B27";
  private InterstitialAd interstitialAd;
  private AdView bannerView;
  private LinearLayout layout;
  private AdListener bannerAdListener;
  private AdListener interstitialAdListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mediation);
    layout = findViewById(R.id.adViewHolder);

    MobileAds.initialize(this, APP_ID);
    initListeners();

    findViewById(R.id.buttonMediationBanner).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        loadBannerAd();
      }
    });

    findViewById(R.id.buttonMediationInterstitial).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        loadInterstitialAd();
      }
    });


  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  private void loadInterstitialAd() {
    interstitialAd = new InterstitialAd(MediationActivity.this);
    interstitialAd.setAdUnitId(
        INTERSTITIAL_ADUNIT_ID);
    interstitialAd.setAdListener(interstitialAdListener);

    AdRequest interstitialAdRequest = new AdRequest.Builder()
        .addTestDevice(TESTDEVICE_ID)
        .build();
    interstitialAd.loadAd(interstitialAdRequest);
  }

  private void loadBannerAd() {

    bannerView = new AdView(MediationActivity.this);
    bannerView.setAdUnitId(BANNER_ADUNIT_ID);
    bannerView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
    bannerView.setAdListener(bannerAdListener);

    AdRequest bannerAdRequest = new AdRequest.Builder()
        .addTestDevice(TESTDEVICE_ID)
        .build();
    bannerView.loadAd(bannerAdRequest);

  }

  private void initListeners() {

    bannerAdListener = new AdListener() {
      @Override
      public void onAdClicked() {
        super.onAdClicked();
        Log.d(TAG, "Mediation - Banner onAdClicked");
      }


      @Override
      public void onAdLeftApplication() {
        super.onAdLeftApplication();
        Log.d(TAG, "Mediation - Banner onAdClicked");
      }

      @Override
      public void onAdFailedToLoad(int errorCode) {
        Log.d(TAG, "Mediation - Banner onAdFailedToLoad");

      }

      @Override
      public void onAdLoaded() {
        Log.d(TAG, "Mediation - Banner onAdLoaded");

        layout.addView(bannerView);

      }

      @Override
      public void onAdOpened() {
        Log.d(TAG, "Mediation - Banner onAdOpened");
      }

      @Override
      public void onAdClosed() {
        Log.d(TAG, "Mediation - Banner onAdClosed");
      }
    };

    interstitialAdListener = new AdListener() {
      @Override
      public void onAdClicked() {
        super.onAdClicked();
        Log.d(TAG, "Mediation - Interstitial onAdClicked");
      }

      @Override
      public void onAdLeftApplication() {
        super.onAdLeftApplication();
        Log.d(TAG, "Mediation - Interstitial onAdLeftApplication");
      }

      @Override
      public void onAdFailedToLoad(int errorCode) {
        Log.d(TAG, "Mediation - Interstitial onAdFailedToLoad");

      }

      @Override
      public void onAdLoaded() {
        Log.d(TAG, "Mediation - Interstitial onAdLoaded");
        if (interstitialAd != null) {
          interstitialAd.show();
        }
      }

      @Override
      public void onAdOpened() {
        Log.d(TAG, "Mediation - Interstitial onAdOpened");
      }

      @Override
      public void onAdClosed() {
        Log.d(TAG, "Mediation - Interstitial onAdClosed");
      }
    };
  }

}

