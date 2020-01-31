package com.criteo.pubsdk_android;

import static com.criteo.pubsdk_android.PubSdkDemoApplication.INTERSTITIAL_IBV_DEMO;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.pubsdk_android.listener.TestAppBannerAdListener;
import com.criteo.pubsdk_android.listener.TestAppInterstitialAdDisplayListener;
import com.criteo.pubsdk_android.listener.TestAppInterstitialAdListener;


public class StandaloneActivity extends AppCompatActivity {

    private static final String TAG = StandaloneActivity.class.getSimpleName();

    private static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
        "/140800857/Endeavour_Interstitial_320x480");

    private static final BannerAdUnit BANNER = new BannerAdUnit(
        "/140800857/Endeavour_320x50",
        new AdSize(320, 50));

    private Context context;
    private LinearLayout adLayout;
    private CriteoBannerView criteoBannerView;
    private Button btnShowInterstitial;
    private Button btnShowInterstitialIbv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_alone);

        adLayout = findViewById(R.id.AdLayout);
        btnShowInterstitial = findViewById(R.id.buttonStandAloneInterstitial);
        btnShowInterstitialIbv = findViewById(R.id.buttonStandAloneInterstitialIbv);

        context = getApplicationContext();

        findViewById(R.id.buttonStandAloneBanner).setOnClickListener(v -> loadBanner());
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
        btnShowInterstitial.setEnabled(false);
        btnShowInterstitialIbv.setEnabled(false);
        loadInterstitial(INTERSTITIAL, btnShowInterstitial);
        loadInterstitial(INTERSTITIAL_IBV_DEMO, btnShowInterstitialIbv);
    }

    private void showInterstitial(CriteoInterstitial interstitial) {
        if (interstitial.isAdLoaded()) {
            interstitial.show();
        }
    }

    private void loadBanner() {
        if (criteoBannerView != null) {
            criteoBannerView.destroy();
        }

        criteoBannerView = new CriteoBannerView(context, BANNER);
        criteoBannerView.setCriteoBannerAdListener(new TestAppBannerAdListener(
            TAG, "Standalone", adLayout, criteoBannerView));

        Log.d(TAG, "Banner Requested");
        criteoBannerView.loadAd();
    }

    private void loadInterstitial(InterstitialAdUnit adUnit, Button btnShow) {
        String prefix = "Standalone " + adUnit.getAdUnitId();

        CriteoInterstitial criteoInterstitial = new CriteoInterstitial(context, adUnit);
        criteoInterstitial.setCriteoInterstitialAdListener(
            new TestAppInterstitialAdListener(TAG, prefix, btnShow));
        criteoInterstitial.setCriteoInterstitialAdDisplayListener(
            new TestAppInterstitialAdDisplayListener(TAG, prefix));

        btnShow.setOnClickListener(v -> showInterstitial(criteoInterstitial));

        Log.d(TAG, prefix + "Interstitial Requested");
        criteoInterstitial.loadAd();
    }

}
