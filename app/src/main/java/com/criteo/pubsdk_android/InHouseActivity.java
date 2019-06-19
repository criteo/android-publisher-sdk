package com.criteo.pubsdk_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.BidResponse;

public class InHouseActivity extends AppCompatActivity {

    private static final String TAG = InHouseActivity.class.getSimpleName();

    private Context context;
    private LinearLayout adLayout;
    private CriteoBannerAdListener criteoBannerAdListener;
    private CriteoBannerView criteoBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_house);

        adLayout = findViewById(R.id.AdLayout);
        context = getApplicationContext();

        createAdListener();

        findViewById(R.id.buttonStandAloneBanner).setOnClickListener(new OnClickListener() {
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

    }

}
