package com.criteo.pubsdk_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.criteo.mediation.listener.CriteoBannerEventListenerImpl;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class StandaloneActivity extends AppCompatActivity {

    private static final String TAG = StandaloneActivity.class.getSimpleName();
    private CustomEventBannerListener customEventBannerListener;
    private LinearLayout adLayout;
    private CriteoBannerView criteoBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_alone);

        adLayout = findViewById(R.id.AdLayout);

        Context context = getApplicationContext();

        createAdListener();

        findViewById(R.id.buttonStandAloneBanner).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adLayout.removeAllViews();
                AdUnit adUnit = new AdUnit();
                adUnit.setPlacementId("/140800857/Endeavour_320x50");
                adUnit.setSize(new AdSize(50, 320));
                criteoBannerView = new CriteoBannerView(context, adUnit);
                CriteoBannerEventListenerImpl criteoBannerEventListener = new CriteoBannerEventListenerImpl(
                        customEventBannerListener,
                        criteoBannerView);
                criteoBannerView.setCriteoBannerAdListener(criteoBannerEventListener);
                criteoBannerView.loadAd();
                adLayout.addView(criteoBannerView);
            }
        });
    }

    private void createAdListener() {
        customEventBannerListener = new CustomEventBannerListener() {
            @Override
            public void onAdLoaded(View view) {
                Log.d(TAG, "Add loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(TAG, "Add failed");
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdClosed() {

            }

            @Override
            public void onAdLeftApplication() {

            }
        };
    }
}
