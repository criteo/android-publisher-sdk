package com.criteo.pubsdk_android;

import static com.criteo.pubsdk_android.PubSdkDemoApplication.INTERSTITIAL_IBV_DEMO;
import static com.criteo.pubsdk_android.PubSdkDemoApplication.NATIVE;

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
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.pubsdk_android.listener.TestAppBannerAdListener;
import com.criteo.pubsdk_android.listener.TestAppInterstitialAdDisplayListener;
import com.criteo.pubsdk_android.listener.TestAppInterstitialAdListener;
import com.criteo.pubsdk_android.listener.TestAppNativeAdListener;

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
  private CriteoNativeLoader nativeLoader;
  private Button btnShowInterstitial;
  private Button btnShowInterstitialIbv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_in_house);

    adLayout = findViewById(R.id.AdLayout);
    btnShowInterstitial = findViewById(R.id.buttonInhouseInterstitial);
    btnShowInterstitialIbv = findViewById(R.id.buttonInhouseInterstitialIbv);
    context = getApplicationContext();

    nativeLoader = new CriteoNativeLoader(
        NATIVE,
        new TestAppNativeAdListener(TAG, NATIVE.getAdUnitId(), adLayout)
    );

    findViewById(R.id.buttonInhouseBanner).setOnClickListener(v -> loadBannerAd());
    findViewById(R.id.buttonInhouseNative).setOnClickListener(v -> loadNative());
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
    loadInterstitialAd(INTERSTITIAL, btnShowInterstitial);
    loadInterstitialAd(INTERSTITIAL_IBV_DEMO, btnShowInterstitialIbv);
  }

  private void showInterstitial(CriteoInterstitial interstitial) {
    if (interstitial.isAdLoaded()) {
      interstitial.show();
    }
  }

  private void loadBannerAd() {
    if (criteoBannerView != null) {
      criteoBannerView.destroy();
    }

    criteoBannerView = new CriteoBannerView(context, BANNER);
    criteoBannerView.setCriteoBannerAdListener(new TestAppBannerAdListener(
        TAG, "In-House", adLayout));

    Log.d(TAG, "Banner Requested");
    BidResponse bidResponse = Criteo.getInstance().getBidResponse(BANNER);
    criteoBannerView.loadAd(bidResponse.getBidToken());
  }

  private void loadNative() {
    BidResponse bidResponse = Criteo.getInstance().getBidResponse(NATIVE);
    nativeLoader.loadAd(bidResponse.getBidToken());
  }

  private void loadInterstitialAd(InterstitialAdUnit adUnit, Button btnShow) {
    String prefix = "In-House " + adUnit.getAdUnitId();

    CriteoInterstitial interstitial = new CriteoInterstitial(context, adUnit);
    interstitial.setCriteoInterstitialAdListener(
        new TestAppInterstitialAdListener(TAG, prefix, btnShow));
    interstitial.setCriteoInterstitialAdDisplayListener(
        new TestAppInterstitialAdDisplayListener(TAG, prefix));

    btnShow.setOnClickListener(v -> showInterstitial(interstitial));

    Log.d(TAG, prefix + " - Interstitial Requested");
    BidResponse bidResponse = Criteo.getInstance().getBidResponse(adUnit);
    interstitial.loadAd(bidResponse.getBidToken());
  }

}
