package com.criteo.pubsdk_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;


public class StandaloneActivity extends AppCompatActivity {

    private static final String TAG = StandaloneActivity.class.getSimpleName();

    private Context context;
    private CriteoBannerAdListener criteoBannerAdListener;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;
    private LinearLayout adLayout;
    private CriteoBannerView criteoBannerView;
    private CriteoInterstitial criteoInterstitial;
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
                BannerAdUnit bannerAdUnit = new BannerAdUnit("/140800857/Endeavour_320x50",
                        new AdSize(320, 50));
                if (criteoBannerView != null) {
                    criteoBannerView.destroy();
                }
                criteoBannerView = new CriteoBannerView(context, bannerAdUnit);
                criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
                Bannerasync bannerasync = new Bannerasync(criteoBannerView);
                bannerasync.execute();

            }
        });

        buttonStandAloneInterstitial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (criteoInterstitial.isAdLoaded()) {
                    criteoInterstitial.show();
                }
            }
        });
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
        buttonStandAloneInterstitial.setEnabled(false);
        interstitialAdLoad();
    }

    private void interstitialAdLoad() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_Interstitial_320x480");
        criteoInterstitial = new CriteoInterstitial(context, interstitialAdUnit);
        criteoInterstitial.setCriteoInterstitialAdListener(criteoInterstitialAdListener);
        criteoInterstitial.loadAd();
    }

    private void createAdListener() {
        criteoBannerAdListener = new CriteoBannerAdListener() {
            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "Banner ad clicked");
            }

            @Override
            public void onAdFailedToLoad(CriteoErrorCode code) {
                Log.d(TAG, "Banner ad failed, reason : " + code.toString());
            }

            @Override
            public void onAdLoaded(View view) {
                Log.d(TAG, "Banner ad loaded");
            }
        };

        criteoInterstitialAdListener = new CriteoInterstitialAdListener() {
            @Override
            public void onAdLoaded() {
                buttonStandAloneInterstitial.setEnabled(true);
                Log.d(TAG, "Interstitial ad loaded");
            }

            @Override
            public void onAdFailedToLoad(CriteoErrorCode code) {
                Log.d(TAG, "Interstitial ad failed");
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "Interstitial ad clicked");
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

    private class Bannerasync extends AsyncTask<Void, Void, Void> {

        private CriteoBannerView bannerView;

        Bannerasync(CriteoBannerView bannerView) {
            this.bannerView = bannerView;
        }

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... voids) {
            bannerView.loadAd();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adLayout.removeAllViews();
            adLayout.addView(bannerView);
        }
    }
}
