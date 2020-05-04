package com.criteo.pubsdk_android.listener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdListener;
import com.criteo.pubsdk_android.R;

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
  public void onAdReceived(CriteoNativeAd nativeAd) {
    Log.d(tag, prefix + " - Native onAdReceived");

    LayoutInflater inflater = LayoutInflater.from(adLayout.getContext());
    View view = inflater.inflate(R.layout.native_ad, null);

    view.<TextView>findViewById(R.id.ad_headline).setText(nativeAd.getTitle());
    view.<TextView>findViewById(R.id.ad_body).setText(nativeAd.getDescription());
    view.<TextView>findViewById(R.id.ad_price).setText(nativeAd.getPrice());
    view.<TextView>findViewById(R.id.ad_call_to_action).setText(nativeAd.getCallToAction());
    view.<TextView>findViewById(R.id.ad_advertiser).setText(nativeAd.getAdvertiserDomain());
    view.<TextView>findViewById(R.id.ad_store).setText(nativeAd.getAdvertiserDescription());

    adLayout.removeAllViews();
    adLayout.addView(view);
  }

  @Override
  public void onAdFailedToReceive(CriteoErrorCode code) {
    Log.d(tag, prefix + " - Native onAdFailedToReceive, reason : " + code.toString());
  }

}
