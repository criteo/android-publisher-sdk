package com.criteo.pubsdk_android.listener;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoErrorCode;

public class TestAppBannerAdListener implements CriteoBannerAdListener {

  private final String tag;
  private final String prefix;
  private final ViewGroup adLayout;

  public TestAppBannerAdListener(String tag, String prefix, ViewGroup adLayout) {
    this.tag = tag;
    this.prefix = prefix;
    this.adLayout = adLayout;
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - Banner onAdLeftApplication");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - Banner onAdClicked");
  }

  @Override
  public void onAdOpened() {
    Log.d(tag, prefix + " - Banner onAdOpened");
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - Banner onAdClosed");
  }

  @Override
  public void onAdFailedToReceive(CriteoErrorCode code) {
    Log.d(tag, prefix + " - Banner onAdFailedToReceive, reason : " + code.toString());
  }

  @Override
  public void onAdReceived(View view) {
    Log.d(tag, prefix + " - Banner onAdReceived");

    adLayout.removeAllViews();
    adLayout.addView(view);
  }

}
