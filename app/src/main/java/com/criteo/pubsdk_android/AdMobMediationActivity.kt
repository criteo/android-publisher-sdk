package com.criteo.pubsdk_android

import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.criteo.pubsdk_android.listener.TestAppDfpAdListener
import com.google.android.gms.ads.*


class AdMobMediationActivity: AppCompatActivity() {

  private companion object {
    /** This AdMob AdUnit is mapped to this Criteo AdUnit: /140800857/Endeavour_320x50 */
    const val ADMOB_BANNER = "ca-app-pub-8459323526901202/2832836926"
  }

  private val tag = javaClass.simpleName

  private lateinit var adLayout: ViewGroup

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_admob_mediation)
    adLayout = findViewById(R.id.adLayout)

    initializeAdMobSdk()

    findViewById<Button>(R.id.buttonAdMobMediationBanner).setOnClickListener { loadBanner() }
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

}