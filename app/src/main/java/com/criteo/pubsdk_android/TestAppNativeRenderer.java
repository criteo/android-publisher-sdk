package com.criteo.pubsdk_android;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.criteo.publisher.advancednative.CriteoNativeAd;
import com.criteo.publisher.advancednative.CriteoNativeRenderer;
import com.criteo.publisher.advancednative.RendererHelper;

public class TestAppNativeRenderer implements CriteoNativeRenderer {

  @NonNull
  @Override
  public View createNativeView(@NonNull Context context, @Nullable ViewGroup parent) {
    LayoutInflater inflater = LayoutInflater.from(context);
    return inflater.inflate(R.layout.native_ad, parent, false);
  }

  @Override
  public void renderNativeView(
      @NonNull RendererHelper helper,
      @NonNull View nativeView,
      @NonNull CriteoNativeAd nativeAd
  ) {
    nativeView.<TextView>findViewById(R.id.ad_headline).setText(nativeAd.getTitle());
    nativeView.<TextView>findViewById(R.id.ad_body).setText(nativeAd.getDescription());
    nativeView.<TextView>findViewById(R.id.ad_price).setText(nativeAd.getPrice());
    nativeView.<TextView>findViewById(R.id.ad_call_to_action).setText(nativeAd.getCallToAction());
    nativeView.<TextView>findViewById(R.id.ad_advertiser).setText(nativeAd.getAdvertiserDomain());
    nativeView.<TextView>findViewById(R.id.ad_store).setText(nativeAd.getAdvertiserDescription());

    helper.setMediaInView(nativeAd.getProductMedia(), nativeView.findViewById(R.id.ad_media));
    helper.setMediaInView(nativeAd.getAdvertiserLogoMedia(), nativeView.findViewById(R.id.ad_app_icon));
  }
}
