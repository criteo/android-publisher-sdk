/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.testapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    nativeView.<TextView>findViewById(R.id.ad_legal_text).setText(nativeAd.getLegalText());

    helper.setMediaInView(nativeAd.getProductMedia(), nativeView.findViewById(R.id.ad_media));
    helper.setMediaInView(nativeAd.getAdvertiserLogoMedia(), nativeView.findViewById(R.id.ad_app_icon));
  }
}
