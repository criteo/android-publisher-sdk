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

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.Criteo.Builder;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.context.EmailHasher;
import com.criteo.publisher.context.UserData;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import java.util.ArrayList;
import java.util.List;
import leakcanary.LeakCanary;
import leakcanary.LeakCanary.Config;

public class PubSdkDemoApplication extends MultiDexApplication {

  private static final String TAG = PubSdkDemoApplication.class.getSimpleName();
  public static final String MOPUB_BANNER_ADUNIT_ID = "b5acf501d2354859941b13030d2d848a";
  public static final String MOPUB_INTERSTITIAL_ADUNIT_ID = "86c36b6223ce4730acf52323de3baa93";
  public static final String MOPUB_NATIVE_ADUNIT_ID = "a298abc2fdf744cf898791831509cc38";

  public static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
      "/140800857/Endeavour_Interstitial_320x480"
  );

  public static final InterstitialAdUnit INTERSTITIAL_IBV_DEMO = new InterstitialAdUnit(
      "mf2v6pikq5vqdjdtfo3j"
  );

  public static final NativeAdUnit NATIVE = new NativeAdUnit("/140800857/Endeavour_Native");

  public static final BannerAdUnit BANNER = new BannerAdUnit(
      "/140800857/Endeavour_320x50",
      new AdSize(320, 50)
  );

  public static final ContextData CONTEXT_DATA = new ContextData()
      .set(ContextData.CONTENT_URL, "https://dummy.content.url");

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

    // Enable leak canary
    // JUnit is in the classpath through the test-utils module. So LeakCanary is deactivated automatically just after
    // the Application#onCreate.
    // Then we need to re-enable it explicitly.
    new Handler().post(() -> {
      Config newConfig = LeakCanary.getConfig().newBuilder()
          .dumpHeap(true)
          .build();

      LeakCanary.setConfig(newConfig);
    });

    List<AdUnit> adUnits = new ArrayList<>();
    adUnits.add(BANNER);
    adUnits.add(INTERSTITIAL);
    adUnits.add(new BannerAdUnit(MOPUB_BANNER_ADUNIT_ID, new AdSize(320, 50)));
    adUnits.add(new InterstitialAdUnit(MOPUB_INTERSTITIAL_ADUNIT_ID));
    adUnits.add(new NativeAdUnit(MOPUB_NATIVE_ADUNIT_ID));
    adUnits.add(INTERSTITIAL_IBV_DEMO);
    adUnits.add(NATIVE);

    try {
      Builder builder = new Builder(this, "B-056946")
          .adUnits(adUnits);

      //noinspection ConstantConditions
      if ("release".equals(BuildConfig.BUILD_TYPE)) {
        // Enable debug logs only on release build.
        // As debug and staging already have a default min log level set to debug, activating the feature would degrade
        // the logs.
        builder.debugLogsEnabled(true);
      }

      Criteo criteo = builder
          .init();

      criteo.setUserData(new UserData()
          .set(UserData.HASHED_EMAIL, EmailHasher.hash("john.doe@gmail.com"))
          .set(UserData.DEV_USER_ID, "devUserId"));
    } catch (Throwable tr) {
      Log.e(TAG, "FAILED TO INIT SDK!!!!", tr);
      throw new IllegalStateException("Criteo SDK is not initialized. You may not proceed.", tr);
    }
  }
}
