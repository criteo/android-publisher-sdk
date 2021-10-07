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

package com.criteo.testapp.listener

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class TestAppDfpRewardedAdListener(private val tag: String, private val activity: Activity) : RewardedAdLoadCallback() {
  override fun onAdLoaded(rewardedAd: RewardedAd) {
    log("onAdLoaded", rewardedAd)

    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdFailedToShowFullScreenContent(adError: AdError) = log("onAdShowedFullScreenContent", adError)
      override fun onAdShowedFullScreenContent() = log("onAdShowedFullScreenContent")
      override fun onAdDismissedFullScreenContent() = log("onAdDismissedFullScreenContent")
      override fun onAdImpression() = log("onAdImpression")
    }

    rewardedAd.show(activity) {
      rewardItem -> log("onUserEarnedReward", rewardItem)
    }
  }

  override fun onAdFailedToLoad(loadAdError: LoadAdError) {
    log("onAdFailedToLoad", loadAdError)
  }

  private fun log(methodName: String, vararg args: Any) {
    Log.d(tag, "RewardedVideo - $methodName(${args.joinToString(", ")})")
  }
}
