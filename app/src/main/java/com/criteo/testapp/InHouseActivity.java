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

import static com.criteo.testapp.PubSdkDemoApplication.BANNER;
import static com.criteo.testapp.PubSdkDemoApplication.INTERSTITIAL;
import static com.criteo.testapp.PubSdkDemoApplication.INTERSTITIAL_IBV_DEMO;
import static com.criteo.testapp.PubSdkDemoApplication.NATIVE;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.publisher.Bid;
import com.criteo.publisher.BidResponseListener;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.advancednative.CriteoNativeLoader;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.testapp.integration.MockedIntegrationRegistry;
import com.criteo.testapp.listener.TestAppBannerAdListener;
import com.criteo.testapp.listener.TestAppInterstitialAdListener;
import com.criteo.testapp.listener.TestAppNativeAdListener;
import java.lang.ref.WeakReference;

public class InHouseActivity extends AppCompatActivity {

  private static final String TAG = InHouseActivity.class.getSimpleName();

  private CriteoBannerView criteoBannerView;
  private FrameLayout nativeAdContainer;
  private CriteoNativeLoader nativeLoader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_in_house);
    MockedIntegrationRegistry.force(Integration.IN_HOUSE);

    criteoBannerView = findViewById(R.id.criteoBannerView);
    nativeAdContainer = findViewById(R.id.nativeAdContainer);
    criteoBannerView.setCriteoBannerAdListener(new TestAppBannerAdListener(
        TAG, "In-House"));

    nativeLoader = new CriteoNativeLoader(
        new TestAppNativeAdListener(TAG, NATIVE.getAdUnitId(), nativeAdContainer),
        new TestAppNativeRenderer()
    );

    findViewById(R.id.buttonInhouseBanner).setOnClickListener(v -> loadBannerAd());
    findViewById(R.id.buttonInhouseNative).setOnClickListener(v -> loadNative());
    findViewById(R.id.buttonInhouseInterstitial).setOnClickListener(v -> loadInterstitial(INTERSTITIAL));
    findViewById(R.id.buttonInhouseInterstitialIbv).setOnClickListener(v -> loadInterstitial(INTERSTITIAL_IBV_DEMO));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (criteoBannerView != null) {
      criteoBannerView.destroy();
    }
  }

  private void loadBannerAd() {
    Log.d(TAG, "Banner Requested");
    Criteo.getInstance().loadBid(BANNER, loadAd(criteoBannerView, CriteoBannerView::loadAd));
  }

  private void loadNative() {
    Criteo.getInstance().loadBid(NATIVE, loadAd(nativeLoader, CriteoNativeLoader::loadAd));
  }

  private void loadInterstitial(InterstitialAdUnit adUnit) {
    String prefix = "In-House " + adUnit.getAdUnitId();

    CriteoInterstitial interstitial = new CriteoInterstitial(adUnit);
    interstitial.setCriteoInterstitialAdListener(new TestAppInterstitialAdListener(TAG, prefix));

    Log.d(TAG, prefix + "Interstitial Requested");
    Criteo.getInstance().loadBid(adUnit, interstitial::loadAd);
  }

  private <T> BidResponseListener loadAd(@NonNull T adLoader, @NonNull BiConsumer<T, Bid> loadAdAction) {
    WeakReference<T> weakAdLoader = new WeakReference<>(adLoader);
    return bid -> {
      T loader = weakAdLoader.get();
      if (loader != null) {
        loadAdAction.accept(loader, bid);
      }
    };
  }

}
