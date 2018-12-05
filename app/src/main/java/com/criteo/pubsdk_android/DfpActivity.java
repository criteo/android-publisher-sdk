package com.criteo.pubsdk_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

public class DfpActivity extends AppCompatActivity {

  private PublisherInterstitialAd mPublisherInterstitialAd;
  private PublisherAdView mPublisherAdView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dfp);
    mPublisherAdView = findViewById(R.id.publisherAdView);
    PublisherAdRequest adRequest = new PublisherAdRequest.Builder()
        .build();
    mPublisherAdView.loadAd(adRequest);

    findViewById(R.id.buttonBanner).setOnClickListener((View v) -> {
      onBannerClick();
    });
    findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> {
      onInterstitialClick();
    });
  }

  private void onBannerClick() {
    mPublisherAdView.setVisibility(View.VISIBLE);
  }

  private void onInterstitialClick() {
    mPublisherAdView.setVisibility(View.GONE);
    mPublisherInterstitialAd = new PublisherInterstitialAd(this);
    mPublisherInterstitialAd.setAdUnitId("/6499/example/interstitial");
    mPublisherInterstitialAd
        .loadAd(new PublisherAdRequest.Builder().build());
    mPublisherInterstitialAd.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        // Code to be executed when an ad finishes loading.
        Log.d("TAG", "adLoaded.");
        if (mPublisherInterstitialAd.isLoaded()) {
          mPublisherInterstitialAd.show();
        } else {
          Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
      }

      @Override
      public void onAdFailedToLoad(int errorCode) {
        // Code to be executed when an ad request fails
        Log.d("TAG", "ad Failed");
      }

      @Override
      public void onAdOpened() {
        // Code to be executed when the ad is displayed.
        Log.d("TAG", "ad Opened");
      }

      @Override
      public void onAdLeftApplication() {
        // Code to be executed when the user has left the app.
        Log.d("TAG", "Left Application");
      }

      @Override
      public void onAdClosed() {
        // Code to be executed when when the interstitial ad is closed.
      }
    });

  }

}
