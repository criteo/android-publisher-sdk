package com.criteo.pubsdk_android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.criteo.pubsdk.Criteo;
import com.criteo.pubsdk.model.AdSize;
import com.criteo.pubsdk.model.AdUnit;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

public class DfpActivity extends AppCompatActivity {

    private PublisherInterstitialAd mPublisherInterstitialAd;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfp);
        String consentDatagiven = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("IABConsent_ParsedVendorConsents", consentDatagiven);
        editor.putString("IABConsent_SubjectToGDPR", "1");
        editor.putString("IABConsent_ConsentString", "1");
        editor.apply();
        linearLayout = ((LinearLayout) findViewById(R.id.adViewHolder));
        findViewById(R.id.buttonBanner).setOnClickListener((View v) -> {
            onBannerClick();
        });
        findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> {
            onInterstitialClick();
        });
    }

    private void onBannerClick() {
        linearLayout.setBackgroundColor(Color.RED);
        linearLayout.removeAllViews();
        linearLayout.setVisibility(View.VISIBLE);
        PublisherAdView publisherAdView = new PublisherAdView(this);
        publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
        publisherAdView.setAdUnitId("/140800857/Endeavour_320x50");
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        Criteo criteo = Criteo.init(this, null, 0);
        AdUnit adUnit = new AdUnit();
        adUnit.setPlacementId("/140800857/Endeavour_320x50");
        AdSize adSize = new AdSize();
        adSize.setWidth(320);
        adSize.setHeight(50);
        adUnit.setSize(adSize);
        publisherAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                linearLayout.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        PublisherAdRequest request = criteo.setBidsForAdUnit(builder, adUnit).build();
        publisherAdView.loadAd(request);
        linearLayout.addView(publisherAdView);
    }

    private void onInterstitialClick() {
        mPublisherInterstitialAd = new PublisherInterstitialAd(this);
        mPublisherInterstitialAd.setAdUnitId("/140800857/Endeavour_Interstitial_320x480");
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        Criteo criteo = Criteo.init(this, null, 0);
        AdUnit interstitialAdUnit = new AdUnit();
        interstitialAdUnit.setPlacementId("/140800857/Endeavour_Interstitial_320x480");
        AdSize interstitialadSize = new AdSize();
        interstitialadSize.setWidth(320);
        interstitialadSize.setHeight(480);
        interstitialAdUnit.setSize(interstitialadSize);
        PublisherAdRequest request = criteo.setBidsForAdUnit(builder, interstitialAdUnit).build();
        mPublisherInterstitialAd
                .loadAd(request);
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
                Log.d("TAG", "ad Failed:" + errorCode);
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
