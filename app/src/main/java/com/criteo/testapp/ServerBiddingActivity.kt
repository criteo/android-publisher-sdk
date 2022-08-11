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
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.CriteoInterstitial

class ServerBiddingActivity : AppCompatActivity() {

  private lateinit var criteoBannerView: CriteoBannerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_server_bidding)

    criteoBannerView = CriteoBannerView(baseContext)

    val buttonBanner: Button = findViewById(R.id.buttonBanner)
    buttonBanner.setOnClickListener {
      val bannerViewContainer: LinearLayout = findViewById(R.id.bannerViewContainer)
      criteoBannerView.loadAdWithDisplayData(
          "https://rdi.eu.criteo.com/delivery/rtb/demo/ajs?" +
              "zoneid=1417086&width=300&height=250&ibva=0"
      )
      bannerViewContainer.addView(criteoBannerView)
    }

    val buttonInterstitial: Button = findViewById(R.id.buttonInterstitial)
    buttonInterstitial.setOnClickListener {
      val criteoInterstitial = CriteoInterstitial()
      criteoInterstitial.setCriteoInterstitialAdListener {
        it.show()
      }
      criteoInterstitial.loadAdWithDisplayData(
          "https://rdi.eu.criteo.com/delivery/rtb/demo/ajs?" +
              "zoneid=1417086&width=393&height=759&ibva=1&uaCap=5"
      )
    }
  }

  override fun onDestroy() {
    criteoBannerView.destroy()
    super.onDestroy()
  }
}
