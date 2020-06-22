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

package com.criteo.pubsdk_android

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.criteo.pubsdk_android.listener.TestAppDfpAdListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView


class AdMobMediationActivity: AppCompatActivity() {

  private companion object {
    /** This AdMob AdUnit is mapped to this Criteo AdUnit: /140800857/Endeavour_320x50 */
    const val ADMOB_BANNER = "ca-app-pub-8459323526901202/2832836926"

    /** This AdMob AdUnit is mapped to this Criteo AdUnit: /140800857/Endeavour_320x480 */
    const val ADMOB_INTERSTITIAL = "ca-app-pub-8459323526901202/6462812944"

    /** This AdMob AdUnit is mapped to this Criteo AdUnit: /140800857/Endeavour_Native */
    const val ADMOB_NATIVE = "ca-app-pub-8459323526901202/2863808899"
  }

  private val tag = javaClass.simpleName

  private lateinit var adLayout: ViewGroup

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_admob_mediation)
    adLayout = findViewById(R.id.adLayout)

    initializeAdMobSdk()

    findViewById<Button>(R.id.buttonAdMobMediationBanner).setOnClickListener { loadBanner() }
    findViewById<Button>(R.id.buttonAdMobMediationInterstitial).setOnClickListener { loadInterstitial() }
    findViewById<Button>(R.id.buttonAdMobMediationNative).setOnClickListener { loadNative() }
  }

  private fun initializeAdMobSdk() {
    // Always declare this device as a test one. This is not necessary for emulator, but it is for
    // real device.
    val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    MobileAds.initialize(this)
    MobileAds.setRequestConfiguration(
        RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(deviceId))
            .build()
    )
  }

  private fun loadBanner() {
    val adView = AdView(this)
    adView.adSize = AdSize.BANNER
    adView.adUnitId = ADMOB_BANNER
    adView.adListener = TestAppDfpAdListener(tag, "Banner")

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    adLayout.removeAllViews()
    adLayout.addView(adView)
  }

  private fun loadInterstitial() {
    val interstitialAd = InterstitialAd(this)
    interstitialAd.adUnitId = ADMOB_INTERSTITIAL
    interstitialAd.adListener = object : TestAppDfpAdListener(tag, "Interstitial") {
      override fun onAdLoaded() {
        super.onAdLoaded()

        if (interstitialAd.isLoaded) {
          interstitialAd.show()
        } else {
          Log.d(tag, "The interstitial wasn't loaded yet.")
        }
      }
    }

    val adRequest = AdRequest.Builder().build()
    interstitialAd.loadAd(adRequest)
  }

  private fun loadNative() {
    val adLoader = AdLoader.Builder(this, ADMOB_NATIVE)
        .forUnifiedNativeAd {
          val adView = layoutInflater.inflate(R.layout.native_admob_ad, null) as UnifiedNativeAdView
          it.renderInView(adView)
          adLayout.removeAllViews()
          adLayout.addView(adView)
        }
        .withAdListener(TestAppDfpAdListener(tag, "Native"))
        .build()

    val adRequest = AdRequest.Builder().build()
    adLoader.loadAd(adRequest)
  }

  private fun UnifiedNativeAd.renderInView(nativeView: UnifiedNativeAdView) {
    nativeView.findViewById<TextView>(R.id.ad_headline).text = headline
    nativeView.findViewById<TextView>(R.id.ad_body).text = body
    nativeView.findViewById<TextView>(R.id.ad_price).text = price
    nativeView.findViewById<TextView>(R.id.ad_call_to_action).text = callToAction
    nativeView.findViewById<TextView>(R.id.ad_advertiser).text = advertiser
    nativeView.findViewById<TextView>(R.id.ad_store).text = extras["crtn_advdomain"] as String?
    nativeView.findViewById<ImageView>(R.id.ad_app_icon).setImageDrawable(icon.drawable)

    nativeView.mediaView = nativeView.findViewById(R.id.ad_media)
    nativeView.mediaView.setMediaContent(mediaContent)

    nativeView.setNativeAd(this)
  }

}