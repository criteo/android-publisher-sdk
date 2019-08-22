package com.criteo.pubsdk_android;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;

public class InHouseActivity extends AppCompatActivity {

    private static final String TAG = InHouseActivity.class.getSimpleName();

    private Context context;
    private LinearLayout adLayout;
    private CriteoBannerAdListener criteoBannerAdListener;
    private CriteoBannerView criteoBannerView;
    private CriteoInterstitial criteoInterstitial;
    private Button buttonInhouseInterstitial;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;
    private InterstitialAdUnit interstitialAdUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_house);

        adLayout = findViewById(R.id.AdLayout);
        buttonInhouseInterstitial = findViewById(R.id.buttonInhouseInterstitial);
        context = getApplicationContext();

        createAdListener();
        BannerAdUnit bannerAdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        criteoBannerView = new CriteoBannerView(context, bannerAdUnit);
        criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);

        interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_Interstitial_320x480");
        criteoInterstitial = new CriteoInterstitial(context, interstitialAdUnit);
        criteoInterstitial.setCriteoInterstitialAdListener(criteoInterstitialAdListener);

        findViewById(R.id.buttonInhouseBanner).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BidResponse bidResponse = Criteo.getInstance().getBidResponse(bannerAdUnit);
                criteoBannerView.loadAd(bidResponse.getBidToken());
            }
        });

        buttonInhouseInterstitial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (criteoInterstitial.isAdLoaded()) {
                    criteoInterstitial.show();
                }
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (criteoBannerView != null) {
            criteoBannerView.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        buttonInhouseInterstitial.setEnabled(false);
        interstitialAdLoad();
    }

    private void interstitialAdLoad() {
        BidResponse bidResponse = Criteo.getInstance().getBidResponse(interstitialAdUnit);

        if (bidResponse != null && bidResponse.isBidSuccess()) {
            criteoInterstitial = new CriteoInterstitial(context, interstitialAdUnit);
            criteoInterstitial.setCriteoInterstitialAdListener(criteoInterstitialAdListener);
            criteoInterstitial.loadAd(bidResponse.getBidToken());
        }
    }


    private void createAdListener() {
        criteoBannerAdListener = new CriteoBannerAdListener() {
            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "Banner ad clicked");
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdFailedToReceive(CriteoErrorCode code) {
                Log.d(TAG, "Banner ad failed, reason : " + code.toString());
            }

            @Override
            public void onAdReceived(View view) {
                Log.d(TAG, "Banner ad loaded");
                adLayout.removeAllViews();
                adLayout.addView(criteoBannerView);
            }
        };

        criteoInterstitialAdListener = new CriteoInterstitialAdListener() {
            @Override
            public void onAdReceived() {
                buttonInhouseInterstitial.setEnabled(true);
                Log.d(TAG, "Interstitial ad loaded");
            }

            @Override
            public void onAdFailedToReceive(CriteoErrorCode code) {
                Log.d(TAG, "Interstitial ad failed");
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "Interstitial ad clicked");
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdOpened() {
                Log.d(TAG, "Interstitial ad full screen");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Interstitial ad closed");
            }
        };
    }

}
