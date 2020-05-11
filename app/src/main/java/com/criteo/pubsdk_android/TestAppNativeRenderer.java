package com.criteo.pubsdk_android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeAdHelper;
import com.criteo.publisher.advancednative.CriteoNativeRenderer;

public class TestAppNativeRenderer implements CriteoNativeRenderer {

  @NonNull
  @Override
  public View createNativeView(@NonNull Context context, @Nullable ViewGroup parent) {
    LayoutInflater inflater = LayoutInflater.from(context);
    return inflater.inflate(R.layout.native_ad, parent, false);
  }

  @Override
  public void renderNativeView(@NonNull View nativeView, @NonNull CriteoNativeAd nativeAd) {
    nativeView.<TextView>findViewById(R.id.ad_headline).setText(nativeAd.getTitle());
    nativeView.<TextView>findViewById(R.id.ad_body).setText(nativeAd.getDescription());
    nativeView.<TextView>findViewById(R.id.ad_price).setText(nativeAd.getPrice());
    nativeView.<TextView>findViewById(R.id.ad_call_to_action).setText(nativeAd.getCallToAction());
    nativeView.<TextView>findViewById(R.id.ad_advertiser).setText(nativeAd.getAdvertiserDomain());
    nativeView.<TextView>findViewById(R.id.ad_store).setText(nativeAd.getAdvertiserDescription());

    ImageView adChoiceView = CriteoNativeAdHelper.getAdChoiceView(nativeAd, nativeView);
    if (adChoiceView != null) {
      // Use dummy image while image loading feature is not available
      adChoiceView.setImageResource(android.R.drawable.ic_delete);
    }
  }
}
