package com.criteo.pubsdk_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class MediationActivity extends AppCompatActivity {

    private static final String TAG = MediationActivity.class.getSimpleName();

    private static final String APP_ID = "ca-app-pub-8459323526901202~2792049297";
    private static final String BANNER_ADUNIT_ID = "ca-app-pub-8459323526901202/2832836926";
    private static final String INTERSTITIAL_ADUNIT_ID = "ca-app-pub-8459323526901202/6462812944";
    private static final String TESTDEVICE_ID = "3D7BB698B9A4CAA9F0D8878A5A5E7B27";
    private InterstitialAd interstitialAd;
    private AdView bannerview;
    private LinearLayout layout;
    private AdListener adListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediation);
        layout = findViewById(R.id.adViewHolder);

        MobileAds.initialize(this, APP_ID);

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

        adListener = new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "Mediation - Interstitial ad failed");

            }

            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Mediation - Interstitial ad loaded");
                if (interstitialAd != null) {
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        };

    }

    private void loadInterstitialAd() {
        interstitialAd = new InterstitialAd(MediationActivity.this);
        interstitialAd.setAdUnitId(
                INTERSTITIAL_ADUNIT_ID);
        interstitialAd.setAdListener(adListener);

        AdRequest interstitialAdRequest = new AdRequest.Builder()
                .addTestDevice(TESTDEVICE_ID)
                .build();
        interstitialAd.loadAd(interstitialAdRequest);
    }

    private void loadBannerAd() {
        bannerview = new AdView(MediationActivity.this);
        bannerview.setAdUnitId(BANNER_ADUNIT_ID);
        bannerview.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        bannerview.setAdListener(adListener);

        AdRequest bannerAdRequest = new AdRequest.Builder()
                .addTestDevice(TESTDEVICE_ID)
                .build();
        bannerview.loadAd(bannerAdRequest);
        layout.addView(bannerview);
    }

}

