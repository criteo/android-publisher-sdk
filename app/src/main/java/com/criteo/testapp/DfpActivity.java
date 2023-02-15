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

import static com.criteo.testapp.PubSdkDemoApplication.BANNER;
import static com.criteo.testapp.PubSdkDemoApplication.CONTEXT_DATA;
import static com.criteo.testapp.PubSdkDemoApplication.INTERSTITIAL;
import static com.criteo.testapp.PubSdkDemoApplication.INTERSTITIAL_VIDEO;
import static com.criteo.testapp.PubSdkDemoApplication.NATIVE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.publisher.Bid;
import com.criteo.publisher.BidResponseListener;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.testapp.integration.MockedIntegrationRegistry;
import com.criteo.testapp.listener.TestAppDfpAdListener;
import com.criteo.testapp.listener.TestAppDfpRewardedAdListener;
import com.criteo.testapp.mock.NetworkUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import java.lang.ref.WeakReference;
import java.util.Collections;

public class DfpActivity extends AppCompatActivity {

  private static final String TAG = DfpActivity.class.getSimpleName();
  private static final String DFP_INTERSTITIAL_AD_UNIT_ID = "/140800857/Endeavour_Interstitial_320x480";
  private static final String DFP_BANNER_AD_UNIT_ID = "/140800857/Endeavour_320x50";
  private static final String DFP_NATIVE_AD_UNIT_ID = "/140800857/Endeavour_Native";
  private static final String DFP_INTERSTITIAL_VIDEO_ID = INTERSTITIAL_VIDEO.getAdUnitId();
  private static final String DFP_REWARDED_VIDEO_ID = DFP_INTERSTITIAL_VIDEO_ID; // TODO?

  private LinearLayout linearLayout;
  private Criteo criteo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dfp);
    MockedIntegrationRegistry.force(Integration.GAM_APP_BIDDING);

    criteo = Criteo.getInstance();

    linearLayout = findViewById(R.id.adViewHolder);
    findViewById(R.id.buttonBanner).setOnClickListener((View v) -> onBannerClick());
    findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> onInterstitialClick());
    findViewById(R.id.buttonInterstitialVideo).setOnClickListener((View v) -> onInterstitialVideoClick());
    findViewById(R.id.buttonCustomNative).setOnClickListener((View v) -> onNativeClick());
    findViewById(R.id.buttonRewardedVideo).setOnClickListener((View v) -> onRewardedVideoClick());

    RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList(AdRequest.DEVICE_ID_EMULATOR)).build();
    MobileAds.setRequestConfiguration(requestConfiguration);
  }

  private void onNativeClick() {
    AdManagerAdView adManagerAdView = new AdManagerAdView(DfpActivity.this);
    adManagerAdView.setAdSizes(com.google.android.gms.ads.AdSize.FLUID);
    adManagerAdView.setAdUnitId(DFP_NATIVE_AD_UNIT_ID);
    adManagerAdView.setAdListener(new TestAppDfpAdListener(TAG, "Custom NativeAd"));
    adManagerAdView.setManualImpressionsEnabled(true);

    loadAdView(adManagerAdView, NATIVE);
  }


  private void onBannerClick() {
    AdManagerAdView adManagerAdView = new AdManagerAdView(this);
    adManagerAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
    adManagerAdView.setAdUnitId(DFP_BANNER_AD_UNIT_ID);
    adManagerAdView.setAdListener(new TestAppDfpAdListener(TAG, "Banner"));

    loadAdView(adManagerAdView, BANNER);
  }

  private void loadAdView(AdManagerAdView adManagerAdView, AdUnit adUnit) {
    AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

    criteo.loadBid(adUnit, CONTEXT_DATA, enrich((mThis, bid) -> {
      mThis.criteo.enrichAdObjectWithBid(builder, bid);

      AdManagerAdRequest request = builder.build();
      adManagerAdView.loadAd(request);
      mThis.linearLayout.addView(adManagerAdView);
    }));
  }

  private void onInterstitialClick() {
    onInterstitialClick(DFP_INTERSTITIAL_AD_UNIT_ID, INTERSTITIAL);
  }

  private void onInterstitialVideoClick() {
    onInterstitialClick(DFP_INTERSTITIAL_VIDEO_ID, INTERSTITIAL_VIDEO);
  }

  private void onInterstitialClick(@NonNull String dfpInterstitialAdUnit, @NonNull InterstitialAdUnit criteoInterstitialAdUnit) {
    AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

    criteo.loadBid(criteoInterstitialAdUnit, CONTEXT_DATA, enrich((mThis, bid) -> {
      mThis.criteo.enrichAdObjectWithBid(builder, bid);

      AdManagerAdRequest request = builder.build();

      AdManagerInterstitialAd.load(
          this.getApplicationContext(),
          dfpInterstitialAdUnit,
          request,
          new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd adManagerInterstitialAd) {
              Log.d(TAG,  "Interstitial - called: onAdLoaded");
              adManagerInterstitialAd.show(mThis);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
              Log.d(TAG, "Interstitial - called: onAdFailedToLoad");
            }
          }
      );
    }));
  }

  private void onRewardedVideoClick() {
    NetworkUtil.logCasperRedirectionWarning(TAG);

    AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

    criteo.loadBid(TestAdUnits.REWARDED_PREPROD, CONTEXT_DATA, enrich((mThis, bid) -> {
      mThis.criteo.enrichAdObjectWithBid(builder, bid);

      AdManagerAdRequest request = builder.build();

      RewardedAd.load(
          this.getApplicationContext(),
          DFP_REWARDED_VIDEO_ID,
          request,
          new TestAppDfpRewardedAdListener(TAG, mThis)
      );
    }));
  }

  private BidResponseListener enrich(BiConsumer<DfpActivity, Bid> enrichAction) {
    WeakReference<DfpActivity> weakThis = new WeakReference<>(this);
    return bid -> {
      DfpActivity activity = weakThis.get();
      if (activity != null) {
        enrichAction.accept(activity, bid);
      }
    };
  }

}
