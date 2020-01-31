package com.criteo.pubsdk_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.pubsdk_android.listener.TestAppBannerAdListener;
import com.criteo.pubsdk_android.listener.TestAppInterstitialAdDisplayListener;
import com.criteo.pubsdk_android.listener.TestAppInterstitialAdListener;

public class InHouseActivity extends AppCompatActivity {

    private static final String TAG = InHouseActivity.class.getSimpleName();

    public static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
        "/140800857/Endeavour_Interstitial_320x480");

    public static final BannerAdUnit BANNER = new BannerAdUnit(
        "/140800857/Endeavour_320x50",
        new AdSize(320, 50));

    private Context context;
    private LinearLayout adLayout;
    private CriteoBannerView criteoBannerView;
    private CriteoInterstitial criteoInterstitial;
    private Button btnShowInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_house);

        adLayout = findViewById(R.id.AdLayout);
        btnShowInterstitial = findViewById(R.id.buttonInhouseInterstitial);
        context = getApplicationContext();

        findViewById(R.id.buttonInhouseBanner).setOnClickListener(v -> loadBannerAd());
        btnShowInterstitial.setOnClickListener(v -> showInterstitial());

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
        loadInterstitialAd();
    }

    private void showInterstitial() {
        if (criteoInterstitial.isAdLoaded()) {
            criteoInterstitial.show();
        }
    }

    private void loadBannerAd() {
        if (criteoBannerView != null) {
            criteoBannerView.destroy();
        }

        criteoBannerView = new CriteoBannerView(context, BANNER);
        criteoBannerView.setCriteoBannerAdListener(new TestAppBannerAdListener(
            TAG, "Standalone", adLayout, criteoBannerView));

        Log.d(TAG, "Banner Requested");
        BidResponse bidResponse = Criteo.getInstance().getBidResponse(BANNER);
        criteoBannerView.loadAd(bidResponse.getBidToken());
    }

    private void loadInterstitialAd() {
        criteoInterstitial = new CriteoInterstitial(context, INTERSTITIAL);
        criteoInterstitial.setCriteoInterstitialAdListener(new TestAppInterstitialAdListener(
            TAG, "In-House", btnShowInterstitial));
        criteoInterstitial.setCriteoInterstitialAdDisplayListener(
            new TestAppInterstitialAdDisplayListener(TAG, "In-House"));

        Log.d(TAG, "Interstitial Requested");
        BidResponse bidResponse = Criteo.getInstance().getBidResponse(INTERSTITIAL);
        criteoInterstitial.loadAd(bidResponse.getBidToken());
    }

}
