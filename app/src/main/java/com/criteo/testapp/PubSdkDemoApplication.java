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

import static com.criteo.publisher.TestAdUnits.MOPUB_MEDIATION_BANNER_ADUNIT_ID;
import static com.criteo.publisher.TestAdUnits.MOPUB_MEDIATION_INTERSTITIAL_ADUNIT_ID;
import static com.criteo.publisher.TestAdUnits.MOPUB_MEDIATION_NATIVE_ADUNIT_ID;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.Criteo.Builder;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.context.EmailHasher;
import com.criteo.publisher.context.UserData;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.network.CdbMock;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.ArrayList;
import java.util.List;
import leakcanary.LeakCanary;
import leakcanary.LeakCanary.Config;

public class PubSdkDemoApplication extends MultiDexApplication {

  /**
   * Flag indicating if the test app should use the CdbMock or should target the default SDK preprod/prod URLs
   */
  private static final boolean USE_CDB_MOCK = BuildConfig.DEBUG;

  private static final String TAG = PubSdkDemoApplication.class.getSimpleName();

  public static final InterstitialAdUnit INTERSTITIAL = TestAdUnits.INTERSTITIAL_PREPROD;
  public static final InterstitialAdUnit INTERSTITIAL_IBV_DEMO = TestAdUnits.INTERSTITIAL_IBV_DEMO;
  public static final InterstitialAdUnit INTERSTITIAL_VIDEO = TestAdUnits.INTERSTITIAL_VIDEO_PREPROD;
  public static final NativeAdUnit NATIVE = TestAdUnits.NATIVE_PREPROD;
  public static final BannerAdUnit BANNER = TestAdUnits.BANNER_320_50_PREPROD;

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

    if (USE_CDB_MOCK) {
      CdbMock cdbMock = new CdbMock(DependencyProvider.getInstance().provideJsonSerializer());
      cdbMock.start();
      System.setProperty(BuildConfigWrapper.CDB_URL_PROP, cdbMock.getUrl());
    }

    List<AdUnit> adUnits = new ArrayList<>();
    adUnits.add(BANNER);
    adUnits.add(INTERSTITIAL);
    adUnits.add(new BannerAdUnit(MOPUB_MEDIATION_BANNER_ADUNIT_ID, new AdSize(320, 50)));
    adUnits.add(new InterstitialAdUnit(MOPUB_MEDIATION_INTERSTITIAL_ADUNIT_ID));
    adUnits.add(new NativeAdUnit(MOPUB_MEDIATION_NATIVE_ADUNIT_ID));
    adUnits.add(INTERSTITIAL_IBV_DEMO);
    adUnits.add(INTERSTITIAL_VIDEO);
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
