package com.criteo.pubsdk_android;

import static com.criteo.pubsdk_android.MopubActivity.initializeMoPubSdk;
import static com.criteo.pubsdk_android.PubSdkDemoApplication.MOPUB_BANNER_ADUNIT_ID;
import static com.criteo.pubsdk_android.PubSdkDemoApplication.MOPUB_INTERSTITIAL_ADUNIT_ID;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import com.criteo.pubsdk_android.listener.TestAppMoPubInterstitialAdListener;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

// TODO: Move this class to the test app repo
public class MopubMediationActivity extends AppCompatActivity {

  private static final String TAG = MopubActivity.class.getSimpleName();
  private MoPubView publisherAdView;
  private LinearLayout linearLayout;

  MoPubInterstitial mInterstitial;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mopub_mediation);

    initializeMoPubSdk(this);

    linearLayout = findViewById(R.id.AdLayout);
    findViewById(R.id.buttonMopubMediationBanner).setOnClickListener((View v) -> onBannerClick());
    findViewById(R.id.buttonMopubMediationInterstitial)
        .setOnClickListener((View v) -> onInterstitialClick());

    mInterstitial = new MoPubInterstitial(this, MOPUB_INTERSTITIAL_ADUNIT_ID);
  }

  private void onBannerClick() {
    linearLayout.setBackgroundColor(Color.RED);
    linearLayout.removeAllViews();
    linearLayout.setVisibility(View.VISIBLE);
    publisherAdView = new MoPubView(this);
    publisherAdView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID);
    publisherAdView.loadAd();
    linearLayout.addView(publisherAdView);
  }

  private void onInterstitialClick() {
    mInterstitial.setInterstitialAdListener(
        new TestAppMoPubInterstitialAdListener(TAG, mInterstitial)
    );

    mInterstitial.load();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    mInterstitial.destroy();
    if (publisherAdView != null) {
      publisherAdView.destroy();
    }
  }
}
