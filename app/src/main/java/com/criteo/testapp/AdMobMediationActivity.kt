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

import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.criteo.publisher.integration.Integration
import com.criteo.testapp.integration.MockedIntegrationRegistry
import com.criteo.testapp.listener.TestAppDfpAdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import java.security.MessageDigest

class AdMobMediationActivity : AppCompatActivity() {

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
    MockedIntegrationRegistry.force(Integration.ADMOB_MEDIATION)
    adLayout = findViewById(R.id.adLayout)

    initializeAdMobSdk()

    findViewById<Button>(R.id.buttonAdMobMediationBanner).setOnClickListener { loadBanner() }
    findViewById<Button>(R.id.buttonAdMobMediationInterstitial).setOnClickListener { loadInterstitial() }
    findViewById<Button>(R.id.buttonAdMobMediationNative).setOnClickListener { loadNative() }
  }

  private fun initializeAdMobSdk() {
    // Always declare this device as a test one. This is not necessary for emulator, but it is for
    // real device.
    // Google requires hashed(MD5) DEVICE_ID
    val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        .toMD5()
        .uppercase()
    MobileAds.setRequestConfiguration(
        RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(deviceId))
            .setTagForChildDirectedTreatment(CoppaActivity.currentCoppaFlag.toGoogleCoppaFlag())
            .build()
    )
    MobileAds.initialize(this)
  }

  private fun loadBanner() {
    val adView = AdView(this)
    adView.setAdSize(AdSize.BANNER)
    adView.adUnitId = ADMOB_BANNER
    adView.adListener = TestAppDfpAdListener(tag, "Banner")

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    adLayout.removeAllViews()
    adLayout.addView(adView)
  }

  private fun loadInterstitial() {
    val activity = this
    InterstitialAd.load(
        activity,
        ADMOB_INTERSTITIAL,
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
          override fun onAdLoaded(interstitialAd: InterstitialAd) {
            interstitialAd.show(activity)
          }
        }
    )
  }

  private fun loadNative() {
    val adLoader = AdLoader.Builder(this, ADMOB_NATIVE)
        .forNativeAd {
          val adView = layoutInflater.inflate(R.layout.native_admob_ad, null) as NativeAdView
          it.renderInView(adView)
          adLayout.removeAllViews()
          adLayout.addView(adView)
        }
        .withAdListener(TestAppDfpAdListener(tag, "Native"))
        .build()

    val adRequest = AdRequest.Builder().build()
    adLoader.loadAd(adRequest)
  }

  private fun NativeAd.renderInView(nativeView: NativeAdView) {
    nativeView.findViewById<TextView>(R.id.ad_headline).text = headline
    nativeView.findViewById<TextView>(R.id.ad_body).text = body
    nativeView.findViewById<TextView>(R.id.ad_price).text = price
    nativeView.findViewById<TextView>(R.id.ad_call_to_action).text = callToAction
    nativeView.findViewById<TextView>(R.id.ad_advertiser).text = advertiser
    nativeView.findViewById<TextView>(R.id.ad_store).text = extras["crtn_advdomain"] as String?
    nativeView.findViewById<ImageView>(R.id.ad_app_icon).setImageDrawable(icon?.drawable)

    nativeView.mediaView = nativeView.findViewById(R.id.ad_media)
    mediaContent?.let { nativeView.mediaView?.setMediaContent(it) }

    nativeView.setNativeAd(this)
  }

  private fun Boolean?.toGoogleCoppaFlag(): Int {
    return when (this) {
      true -> RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
      false -> RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
      else -> RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED
    }
  }

  private fun String.toMD5(): String {
    return trim().run {
      MessageDigest.getInstance("MD5")
          .digest(toByteArray())
          .joinToString("") {
            "%02x".format(it)
          }
    }
  }
}
