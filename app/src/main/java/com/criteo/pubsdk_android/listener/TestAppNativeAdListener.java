package com.criteo.pubsdk_android.listener;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdListener;

public class TestAppNativeAdListener extends CriteoNativeAdListener {

  private final String tag;
  private final String prefix;
  private final ViewGroup adLayout;

  public TestAppNativeAdListener(String tag, String prefix, ViewGroup adLayout) {
    this.tag = tag;
    this.prefix = prefix;
    this.adLayout = adLayout;
  }

  @Override
  public void onAdReceived(@NonNull CriteoNativeAd nativeAd) {
    Log.d(tag, prefix + " - Native onAdReceived");

    View view = nativeAd.createNativeRenderedView(adLayout.getContext(), adLayout);
    adLayout.removeAllViews();
    adLayout.addView(view);
  }

  @Override
  public void onAdFailedToReceive(@NonNull CriteoErrorCode code) {
    Log.d(tag, prefix + " - Native onAdFailedToReceive, reason : " + code.toString());
  }

  @Override
  public void onAdImpression() {
    Log.d(tag, prefix + " - Native onAdImpression");
  }

  @Override
  public void onAdClicked() {
    Log.d(tag, prefix + " - Native onAdClicked");
  }

  @Override
  public void onAdLeftApplication() {
    Log.d(tag, prefix + " - Native onAdLeftApplication");
  }

  @Override
  public void onAdClosed() {
    Log.d(tag, prefix + " - Native onAdClosed");
  }

}
