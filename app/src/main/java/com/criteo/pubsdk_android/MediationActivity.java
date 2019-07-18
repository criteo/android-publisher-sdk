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

    private static final String INTERSTITIAL_ADUNIT_ID = "ca-app-pub-2995206374493561/4891139268";
    private static final String APP_ID = "ca-app-pub-2995206374493561~8272596058";
    private static final String BANNER_ADUNIT_ID = "ca-app-pub-2995206374493561/3062725613";
    private static final String TESTDEVICE_ID = "B86644E365C34D02597C12ED444E060A";

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

