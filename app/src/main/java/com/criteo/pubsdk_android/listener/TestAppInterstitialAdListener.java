package com.criteo.pubsdk_android.listener;

import android.util.Log;
import android.widget.Button;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdListener;

public class TestAppInterstitialAdListener implements CriteoInterstitialAdListener {

  private final String tag;
  private final String prefix;
  private final Button btnShowInterstitial;

  public TestAppInterstitialAdListener(String tag, String prefix,
      Button btnShowInterstitial) {
    this.tag = tag;
    this.prefix = prefix;
    this.btnShowInterstitial = btnShowInterstitial;
  }

  @Override
  public void onAdReceived() {
    btnShowInterstitial.setEnabled(true);
    Log.d(tag, prefix + " - Interstitial onAdReceived");
  }

  @Override
  public void onAdFailedToReceive(CriteoErrorCode code) {
    Log.d(tag, prefix + " - Interstitial onAdFailedToReceive");
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - Interstitial onAdLeftApplication");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - Interstitial onAdClicked");
  }

  @Override
  public void onAdOpened() {
    Log.d(tag, prefix + " - Interstitial onAdOpened");
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - Interstitial onAdClosed");
  }

}
