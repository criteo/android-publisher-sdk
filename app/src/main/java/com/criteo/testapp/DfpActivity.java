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

import static com.criteo.testapp.PubSdkDemoApplication.NATIVE_AD_UNIT_ID;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.publisher.Bid;
import com.criteo.publisher.BidResponseListener;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.testapp.integration.MockedIntegrationRegistry;
import com.criteo.testapp.listener.TestAppDfpAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import java.lang.ref.WeakReference;

public class DfpActivity extends AppCompatActivity {

  private static final String TAG = DfpActivity.class.getSimpleName();
  private static final String DFP_INTERSTITIAL_ID = "/140800857/Endeavour_Interstitial_320x480";
  private static final String DFP_BANNER_ID = "/140800857/Endeavour_320x50";

  private static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
      "/140800857/Endeavour_Interstitial_320x480");

  public static final BannerAdUnit BANNER = new BannerAdUnit(
      "/140800857/Endeavour_320x50",
      new AdSize(320, 50));

  public static final NativeAdUnit NATIVE = new NativeAdUnit(NATIVE_AD_UNIT_ID);

  private LinearLayout linearLayout;
  private Criteo criteo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dfp);
    MockedIntegrationRegistry.force(Integration.GAM_APP_BIDDING);

    criteo = Criteo.getInstance();

    linearLayout = findViewById(R.id.adViewHolder);
    findViewById(R.id.buttonBanner).setOnClickListener((View v) -> {
      onBannerClick();
    });
    findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> {
      onInterstitialClick();
    });
    findViewById(R.id.buttonCustomNative).setOnClickListener((View v) -> {
      onNativeClick();
    });
  }

  private void onNativeClick() {
    PublisherAdView publisherAdView = new PublisherAdView(DfpActivity.this);
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.FLUID);
    publisherAdView.setAdUnitId(NATIVE_AD_UNIT_ID);
    publisherAdView.setAdListener(new TestAppDfpAdListener(TAG, "Custom NativeAd"));
    publisherAdView.setManualImpressionsEnabled(true);

    loadAdView(publisherAdView, NATIVE);
  }


  private void onBannerClick() {
    PublisherAdView publisherAdView = new PublisherAdView(this);
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
    publisherAdView.setAdUnitId(DFP_BANNER_ID);
    publisherAdView.setAdListener(new TestAppDfpAdListener(TAG, "Banner"));

    loadAdView(publisherAdView, BANNER);
  }

  private void loadAdView(PublisherAdView publisherAdView, AdUnit adUnit) {
    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    criteo.loadBid(adUnit, enrich((mThis, bid) -> {
      mThis.criteo.enrichAdObjectWithBid(builder, bid);

      PublisherAdRequest request = builder.build();
      publisherAdView.loadAd(request);
      mThis.linearLayout.addView(publisherAdView);
    }));
  }

  private void onInterstitialClick() {
    PublisherInterstitialAd mPublisherInterstitialAd = new PublisherInterstitialAd(this);
    mPublisherInterstitialAd.setAdUnitId(DFP_INTERSTITIAL_ID);
    mPublisherInterstitialAd.setAdListener(new TestAppDfpAdListener(TAG, "Interstitial") {
      @Override
      public void onAdLoaded() {
        super.onAdLoaded();

        if (mPublisherInterstitialAd.isLoaded()) {
          mPublisherInterstitialAd.show();
        } else {
          Log.d(TAG, "The interstitial wasn't loaded yet.");
        }
      }
    });

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    criteo.loadBid(INTERSTITIAL, enrich((mThis, bid) -> {
      mThis.criteo.enrichAdObjectWithBid(builder, bid);

      PublisherAdRequest request = builder.build();
      mPublisherInterstitialAd.loadAd(request);
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
