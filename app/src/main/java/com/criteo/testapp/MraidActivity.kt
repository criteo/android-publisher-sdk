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
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.CriteoInterstitial
import com.criteo.testapp.PubSdkDemoApplication.Companion.CONTEXT_DATA
import com.criteo.testapp.PubSdkDemoApplication.Companion.MRAID_INTERSTITIAL_DEMO
import com.criteo.testapp.listener.TestAppBannerAdListener
import com.criteo.testapp.listener.TestAppInterstitialAdListener

class MraidActivity : AppCompatActivity(R.layout.activity_mraid) {

  private val tag = MraidActivity::class.java.simpleName

  private lateinit var criteoBannerView: CriteoBannerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    criteoBannerView = findViewById(R.id.criteoBannerView)
    criteoBannerView.setCriteoBannerAdListener(
        TestAppBannerAdListener(
            tag, "Mraid"
        )
    )

    findViewById<Button>(R.id.buttonBanner).setOnClickListener { loadBannerAd() }
    findViewById<Button>(R.id.buttonInterstitial).setOnClickListener { loadInterstitial() }
  }

  private fun loadBannerAd() {
    Log.d(tag, "Banner Requested")
    criteoBannerView.loadAd(CONTEXT_DATA)
  }

  private fun loadInterstitial() {
    val adUnit = MRAID_INTERSTITIAL_DEMO
    val prefix = "Mraid " + adUnit.adUnitId
    val criteoInterstitial = CriteoInterstitial(adUnit)
    criteoInterstitial.setCriteoInterstitialAdListener(
        TestAppInterstitialAdListener(
            tag,
            prefix
        )
    )
    Log.d(tag, prefix + "Interstitial Requested")
    criteoInterstitial.loadAd(CONTEXT_DATA)
  }

  override fun onDestroy() {
    super.onDestroy()
    criteoBannerView.destroy()
  }
}
