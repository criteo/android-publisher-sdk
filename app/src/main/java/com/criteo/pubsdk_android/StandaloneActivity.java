package com.criteo.pubsdk_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.criteo.mediation.listener.CriteoBannerEventListenerImpl;
import com.criteo.mediation.listener.CriteoInterstitialEventListenerImpl;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;
import com.criteo.publisher.model.AdSize;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class StandaloneActivity extends AppCompatActivity {

    private static final String TAG = StandaloneActivity.class.getSimpleName();

    private Context context;
    private CustomEventBannerListener customEventBannerListener;
    private CustomEventInterstitialListener customEventInterstitialListener;
    private LinearLayout adLayout;
    private CriteoBannerView criteoBannerView;
    private CriteoInterstitialView criteoInterstitialView;
    private Button buttonStandAloneInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_alone);

        adLayout = findViewById(R.id.AdLayout);
        buttonStandAloneInterstitial = findViewById(R.id.buttonStandAloneInterstitial);

        context = getApplicationContext();

        createAdListener();

        findViewById(R.id.buttonStandAloneBanner).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adLayout.removeAllViews();
                BannerAdUnit bannerAdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(50, 320));
                criteoBannerView = new CriteoBannerView(context, bannerAdUnit);
                CriteoBannerEventListenerImpl criteoBannerEventListener = new CriteoBannerEventListenerImpl(
                        customEventBannerListener,
                        criteoBannerView);
                criteoBannerView.setCriteoBannerAdListener(criteoBannerEventListener);
                criteoBannerView.loadAd();
                adLayout.addView(criteoBannerView);
            }
        });

        buttonStandAloneInterstitial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (criteoInterstitialView.isAdLoaded()) {
                    criteoInterstitialView.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        buttonStandAloneInterstitial.setEnabled(false);
        interstitialAdLoad();
    }

    private void interstitialAdLoad() {


        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_Interstitial_320x480");
        criteoInterstitialView = new CriteoInterstitialView(context, interstitialAdUnit);

        CriteoInterstitialEventListenerImpl criteoInterstitialEventListener = new CriteoInterstitialEventListenerImpl(
                customEventInterstitialListener, criteoInterstitialView);

        criteoInterstitialView.setCriteoInterstitialAdListener(criteoInterstitialEventListener);

        criteoInterstitialView.loadAd();
    }

    private void createAdListener() {
        customEventBannerListener = new CustomEventBannerListener() {
            @Override
            public void onAdLoaded(View view) {
                Log.d(TAG, "Banner ad loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(TAG, "Banner ad failed");
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Banner ad clicked");
            }

            @Override
            public void onAdClosed() {

            }

            @Override
            public void onAdLeftApplication() {

            }
        };

        customEventInterstitialListener = new CustomEventInterstitialListener() {
            @Override
            public void onAdLoaded() {
                buttonStandAloneInterstitial.setEnabled(true);
                Log.d(TAG, "Interstitial ad loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(TAG, "Interstitial ad failed");
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Interstitial ad clicked");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Interstitial ad closed");
            }

            @Override
            public void onAdLeftApplication() {

            }
        };

    }
}
