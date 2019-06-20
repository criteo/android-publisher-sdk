package com.criteo.pubsdk_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.BidResponse;
import com.criteo.publisher.model.InterstitialAdUnit;

public class InHouseActivity extends AppCompatActivity {

    private static final String TAG = InHouseActivity.class.getSimpleName();

    private Context context;
    private LinearLayout adLayout;
    private CriteoBannerAdListener criteoBannerAdListener;
    private CriteoBannerView criteoBannerView;
    private CriteoInterstitialView criteoInterstitialView;
    private Button buttonInhouseInterstitial;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_house);

        adLayout = findViewById(R.id.AdLayout);
        buttonInhouseInterstitial = findViewById(R.id.buttonInhouseInterstitial);
        context = getApplicationContext();

        createAdListener();

        findViewById(R.id.buttonInhouseBanner).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adLayout.removeAllViews();
                BannerAdUnit bannerAdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(50, 320));

                BidResponse bidResponse = Criteo.getInstance().getBidForInhouseMediation(bannerAdUnit);

                if (bidResponse.isValid()) {
                    criteoBannerView = new CriteoBannerView(context, bannerAdUnit);
                    criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
                    criteoBannerView.loadAd(bidResponse.getToken());
                    adLayout.addView(criteoBannerView);
                }
            }
        });

        buttonInhouseInterstitial.setOnClickListener(new OnClickListener() {
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
        buttonInhouseInterstitial.setEnabled(false);
        interstitialAdLoad();
    }

    private void interstitialAdLoad() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_Interstitial_320x480");
        criteoInterstitialView = new CriteoInterstitialView(context, interstitialAdUnit);
        criteoInterstitialView.setCriteoInterstitialAdListener(criteoInterstitialAdListener);
        criteoInterstitialView.loadAd();
    }


    private void createAdListener() {
        criteoBannerAdListener = new CriteoBannerAdListener() {

            @Override
            public void onAdFullScreen() {
                Log.d(TAG, "Banner ad fullscreen");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Banner ad closed");
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Banner ad clicked");
            }

            @Override
            public void onAdFetchFailed(CriteoErrorCode code) {
                Log.d(TAG, "Banner ad failed, reason : " + code.toString());
            }

            @Override
            public void onAdFetchSucceeded(View view) {
                Log.d(TAG, "Banner ad loaded");
            }
        };

        criteoInterstitialAdListener = new CriteoInterstitialAdListener() {
            @Override
            public void onAdFetchSucceeded() {
                buttonInhouseInterstitial.setEnabled(true);
                Log.d(TAG, "Interstitial ad loaded");
            }

            @Override
            public void onAdFetchFailed(CriteoErrorCode code) {
                Log.d(TAG, "Interstitial ad failed");
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Interstitial ad clicked");
            }

            @Override
            public void onAdFullScreen() {
                Log.d(TAG, "Interstitial ad full screen");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Interstitial ad closed");
            }
        };
    }

}
