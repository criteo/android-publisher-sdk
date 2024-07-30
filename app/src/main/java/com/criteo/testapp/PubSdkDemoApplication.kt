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
package com.criteo.testapp

import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.webkit.WebView
import androidx.multidex.MultiDexApplication
import com.criteo.publisher.Criteo
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.TestAdUnits
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.context.EmailHasher.hash
import com.criteo.publisher.context.UserData
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.InterstitialAdUnit
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.network.CdbMock
import com.criteo.publisher.util.BuildConfigWrapper
import com.criteo.testapp.integration.IntegrationSelectionMode
import com.criteo.testapp.integration.IntegrationSelectorActivity.Companion.mockIntegrationRegistry
import com.criteo.testapp.mock.MockedDependencyProvider
import leakcanary.LeakCanary.config

class PubSdkDemoApplication : MultiDexApplication() {
  companion object {
    private const val CDB_DEMO_BANNER_PLACEMENT_ID = "30s6zt3ayypfyemwjvmp"
    private const val CDB_DEMO_INTERSTITIAL_PLACEMENT_ID = "6yws53jyfjgoq1ghnuqb"
    private const val CDB_DEMO_NATIVE_PLACEMENT_ID = "190tsfngohsvfkh3hmkm"
    private const val CDB_DEMO_MRAID_PLACEMENT_ID = "7fspp28x445grwm378ck"

    @JvmField
    val INTERSTITIAL = InterstitialAdUnit(
        CDB_DEMO_INTERSTITIAL_PLACEMENT_ID
    )

    @JvmField
    val INTERSTITIAL_IBV_DEMO = TestAdUnits.INTERSTITIAL_IBV_DEMO

    @JvmField
    val INTERSTITIAL_VIDEO = TestAdUnits.INTERSTITIAL_VIDEO_PREPROD

    @JvmField
    val NATIVE = NativeAdUnit(
        CDB_DEMO_NATIVE_PLACEMENT_ID
    )

    @JvmField
    val BANNER = BannerAdUnit(
        CDB_DEMO_BANNER_PLACEMENT_ID,
        AdSize(320, 50)
    )

    val MRAID_INTERSTITIAL_DEMO = InterstitialAdUnit(CDB_DEMO_MRAID_PLACEMENT_ID)

    @JvmField
    val CONTEXT_DATA = ContextData().set(ContextData.CONTENT_URL, "https://dummy.content.url")
  }

  override fun onCreate() {
    super.onCreate()
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
    StrictMode.setVmPolicy(
        VmPolicy.Builder().detectAll()
            .penaltyLog()
            .build()
    )

    WebView.setWebContentsDebuggingEnabled(true)

    // Enable leak canary
    // JUnit is in the classpath through the test-utils module. So LeakCanary is deactivated automatically just after
    // the Application#onCreate.
    // Then we need to re-enable it explicitly.
    Handler(Looper.getMainLooper()).post {
      config = config.copy(dumpHeap = true)
    }

    if (BuildConfig.useCdbMock) {
      val cdbMock = CdbMock(DependencyProvider.getInstance().provideJsonSerializer())
      cdbMock.start()
      System.setProperty(BuildConfigWrapper.CDB_URL_PROP, cdbMock.url)
    }

    MockedDependencyProvider.prepareMock(this)
    MockedDependencyProvider.startMocking {
      val mode = if (BuildConfig.doNotDetectMediationAdapter) {
        IntegrationSelectionMode.NoMediation
      } else {
        IntegrationSelectionMode.NoMock
      }
      mockIntegrationRegistry(mode, true)
    }

    val adUnits = listOf(
        BANNER,
        INTERSTITIAL,
        INTERSTITIAL_IBV_DEMO,
        INTERSTITIAL_VIDEO,
        NATIVE
    )

    val builder = Criteo.Builder(this, "B-000000")
        .adUnits(adUnits)
        .inventoryGroupId("myInventoryGroupId")

    if ("release" == BuildConfig.BUILD_TYPE) {
      // Enable debug logs only on release build.
      // As debug and staging already have a default min log level set to debug, activating the feature would degrade
      // the logs.
      builder.debugLogsEnabled(true)
    }

    val criteo = builder.init()

    criteo.setUserData(UserData()
        .set(UserData.HASHED_EMAIL, hash("john.doe@gmail.com"))
        .set(UserData.DEV_USER_ID, "devUserId")
    )
  }
}
