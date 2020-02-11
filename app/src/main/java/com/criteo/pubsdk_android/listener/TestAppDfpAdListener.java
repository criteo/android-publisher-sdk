package com.criteo.pubsdk_android.listener;

import android.util.Log;
import com.google.android.gms.ads.AdListener;

public class TestAppDfpAdListener extends AdListener {

  private final String tag;
  private final String prefix;

  public TestAppDfpAdListener(String tag, String prefix) {
    this.tag = tag;
    this.prefix = prefix;
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - called: onAdClosed");
  }

  @Override
  public void onAdFailedToLoad(int var1) {
    Log.d(tag, prefix + " - called: onAdFailedToLoad");
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - called: onAdLeftApplication");
  }

  @Override
  public void onAdOpened() {
    Log.d(tag, prefix + " - called: onAdOpened");
  }

  @Override
  public void onAdLoaded() {
    Log.d(tag, prefix + " - called: onAdLoaded");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - called: onAdClicked");
  }

  @Override
  public void onAdImpression() {
    Log.d(tag, prefix + " - called: onAdImpression");
  }

}
