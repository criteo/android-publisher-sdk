package com.criteo.pubsdk_android;

import static com.criteo.pubsdk_android.PubSdkDemoApplication.NATIVE_AD_UNIT_ID;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.pubsdk_android.listener.TestAppDfpAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

public class DfpActivity extends AppCompatActivity {

  private static final String TAG = DfpActivity.class.getSimpleName();
  private static final String DFP_INTERSTITIAL_ID = "/140800857/Endeavour_Interstitial_320x480";
  private static final String DFP_BANNER_ID = "/140800857/Endeavour_320x50";

  private static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
      "/140800857/Endeavour_320x50");

  public static final BannerAdUnit BANNER = new BannerAdUnit(
      "/140800857/Endeavour_320x50",
      new AdSize(320, 50));

  public static final NativeAdUnit NATIVE = new NativeAdUnit(NATIVE_AD_UNIT_ID);

  private LinearLayout linearLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dfp);

    linearLayout = findViewById(R.id.adViewHolder);
    findViewById(R.id.buttonBanner).setOnClickListener((View v) -> {
      onBannerClick();
    });
    findViewById(R.id.buttonInterstitial).setOnClickListener((View v) -> {
      onInterstitialClick();
    });
    findViewById(R.id.buttonCustomNative).setOnClickListener((View v) -> {
      onNativeClick();
    });
  }

  private void onNativeClick() {
    PublisherAdView publisherAdView = new PublisherAdView(DfpActivity.this);
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.FLUID);
    publisherAdView.setAdUnitId(NATIVE_AD_UNIT_ID);
    publisherAdView.setAdListener(new TestAppDfpAdListener(TAG, "Custom NativeAd"));
    publisherAdView.setManualImpressionsEnabled(true);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    Criteo.getInstance().setBidsForAdUnit(builder, NATIVE);
    PublisherAdRequest request = builder.build();
    publisherAdView.loadAd(request);
    linearLayout.addView(publisherAdView);
  }


  private void onBannerClick() {
    PublisherAdView publisherAdView = new PublisherAdView(this);
    publisherAdView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);
    publisherAdView.setAdUnitId(DFP_BANNER_ID);
    publisherAdView.setAdListener(new TestAppDfpAdListener(TAG, "Banner"));

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    Criteo.getInstance().setBidsForAdUnit(builder, BANNER);
    PublisherAdRequest request = builder.build();
    publisherAdView.loadAd(request);
    linearLayout.addView(publisherAdView);
  }

  private void onInterstitialClick() {
    PublisherInterstitialAd mPublisherInterstitialAd = new PublisherInterstitialAd(this);
    mPublisherInterstitialAd.setAdUnitId(DFP_INTERSTITIAL_ID);
    mPublisherInterstitialAd.setAdListener(new TestAppDfpAdListener(TAG, "Interstitial") {
      @Override
      public void onAdLoaded() {
        super.onAdLoaded();

        if (mPublisherInterstitialAd.isLoaded()) {
          mPublisherInterstitialAd.show();
        } else {
          Log.d(TAG, "The interstitial wasn't loaded yet.");
        }
      }
    });

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    Criteo.getInstance().setBidsForAdUnit(builder, INTERSTITIAL);
    PublisherAdRequest request = builder.build();
    mPublisherInterstitialAd.loadAd(request);
  }

}
