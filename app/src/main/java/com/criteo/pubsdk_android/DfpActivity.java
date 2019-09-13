package com.criteo.pubsdk_android;

import static com.criteo.pubsdk_android.PubSdkDemoApplication.NATIVE_AD_UNIT_ID;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.NativeAdUnit;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

public class DfpActivity extends AppCompatActivity {

    private PublisherInterstitialAd mPublisherInterstitialAd;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfp);
        String consentDatagiven = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("IABConsent_ParsedVendorConsents", consentDatagiven);
        editor.putString("IABConsent_SubjectToGDPR", "1");
        editor.putString("IABConsent_ConsentString", "1");
        editor.apply();
        linearLayout = ((LinearLayout) findViewById(R.id.adViewHolder));
        findViewById(R.id.buttonBanner).setOnClickListener((View v) -> {
            onBannerClick();
        });
        findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> {
            onInterstitialClick();
        });
        findViewById(R.id.buttonBannerCustomNative).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PublisherAdView publisherAdView = new PublisherAdView(DfpActivity.this);
                publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.FLUID);
                publisherAdView.setAdUnitId(NATIVE_AD_UNIT_ID);
                PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
                builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                NativeAdUnit nativeAdUnit = new NativeAdUnit(NATIVE_AD_UNIT_ID);
                Criteo.getInstance().setBidsForAdUnit(builder, nativeAdUnit);
                PublisherAdRequest request = builder.build();
                publisherAdView.loadAd(request);
                linearLayout.addView(publisherAdView);
            }
        });
    }

    private void onBannerClick() {
        PublisherAdView publisherAdView = new PublisherAdView(this);
        publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
        publisherAdView.setAdUnitId("/140800857/Endeavour_320x50");
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        BannerAdUnit bannerAdUnit = new BannerAdUnit("/140800857/Endeavour_320x50" , new AdSize(320, 50));
        publisherAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }
        });
        Criteo.getInstance().setBidsForAdUnit(builder, bannerAdUnit);
        PublisherAdRequest request = builder.build();
        publisherAdView.loadAd(request);
        linearLayout.addView(publisherAdView);
    }

    private void onInterstitialClick() {
        mPublisherInterstitialAd = new PublisherInterstitialAd(this);
        mPublisherInterstitialAd.setAdUnitId("/140800857/Endeavour_Interstitial_320x480");
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_320x50");
        Criteo.getInstance().setBidsForAdUnit(builder, interstitialAdUnit);
        PublisherAdRequest request = builder.build();
        mPublisherInterstitialAd
                .loadAd(request);
        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d("TAG", "adLoaded.");
                if (mPublisherInterstitialAd.isLoaded()) {
                    mPublisherInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails
                Log.d("TAG", "ad Failed:" + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.d("TAG", "ad Opened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d("TAG", "Left Application");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
            }
        });

    }

}
