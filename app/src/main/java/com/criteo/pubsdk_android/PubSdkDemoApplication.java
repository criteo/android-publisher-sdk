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

package com.criteo.pubsdk_android;

import android.os.StrictMode;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import java.util.ArrayList;
import java.util.List;

public class PubSdkDemoApplication extends MultiDexApplication {

  private static final String TAG = PubSdkDemoApplication.class.getSimpleName();
  public static final String DFP_BANNER_ADUNIT_ID = "/140800857/Endeavour_320x50";
  public static final String DFP_INTERSTITIAL_ADUNIT_ID = "/140800857/Endeavour_Interstitial_320x480";
  public static final String MOPUB_BANNER_ADUNIT_ID = "b5acf501d2354859941b13030d2d848a";
  public static final String MOPUB_INTERSTITIAL_ADUNIT_ID = "86c36b6223ce4730acf52323de3baa93";
  public static final String MOPUB_NATIVE_ADUNIT_ID = "a298abc2fdf744cf898791831509cc38";
  public static final String NATIVE_AD_UNIT_ID = "/140800857/Endeavour_Native";

  public static final InterstitialAdUnit INTERSTITIAL_IBV_DEMO = new InterstitialAdUnit(
      "mf2v6pikq5vqdjdtfo3j");

  public static final NativeAdUnit NATIVE = new NativeAdUnit(NATIVE_AD_UNIT_ID);

  public static final BannerAdUnit STANDALONE_BANNER = new BannerAdUnit(
      DFP_BANNER_ADUNIT_ID,
      new AdSize(320, 50)
  );

  @Override
  public void onCreate() {
    super.onCreate();
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
        .penaltyLog()
        .build());

    List<AdUnit> adUnits = new ArrayList<>();

    BannerAdUnit bannerAdUnit = new BannerAdUnit(DFP_BANNER_ADUNIT_ID, new AdSize(320, 50));
    adUnits.add(bannerAdUnit);

    InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(DFP_INTERSTITIAL_ADUNIT_ID);
    adUnits.add(interstitialAdUnit);

    BannerAdUnit moPubBannerAdUnit = new BannerAdUnit(MOPUB_BANNER_ADUNIT_ID, new AdSize(320, 50));
    adUnits.add(moPubBannerAdUnit);

    InterstitialAdUnit moPubInterstitialAdUnit = new InterstitialAdUnit(
        MOPUB_INTERSTITIAL_ADUNIT_ID);
    adUnits.add(moPubInterstitialAdUnit);

    adUnits.add(new NativeAdUnit(MOPUB_NATIVE_ADUNIT_ID));

    adUnits.add(INTERSTITIAL_IBV_DEMO);
    adUnits.add(NATIVE);

    try {
      Criteo.init(this, "B-056946", adUnits);
    } catch (Throwable tr) {
      Log.e(TAG, "FAILED TO INIT SDK!!!!", tr);
      throw new IllegalStateException("Criteo SDK is not initialized. You may not proceed.", tr);
    }
  }
}
