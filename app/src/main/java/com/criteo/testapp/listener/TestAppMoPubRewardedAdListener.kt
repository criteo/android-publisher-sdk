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

import android.util.Log
import com.mopub.common.MoPubReward
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubRewardedAdListener
import com.mopub.mobileads.MoPubRewardedAds

open class TestAppMoPubRewardedAdListener : MoPubRewardedAdListener {
  override fun onRewardedAdClicked(adUnitId: String) {
    log("onRewardedAdClicked($adUnitId)")
  }

  override fun onRewardedAdClosed(adUnitId: String) {
    log("onRewardedAdClosed($adUnitId)")
  }

  override fun onRewardedAdCompleted(adUnitIds: Set<String?>, reward: MoPubReward) {
    log("onRewardedAdCompleted($adUnitIds, ${reward.toFormattedString()})")
  }

  override fun onRewardedAdLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {
    log("onRewardedAdLoadFailure($adUnitId, $errorCode)")
  }

  override fun onRewardedAdLoadSuccess(adUnitId: String) {
    log("onRewardedAdLoadSuccess($adUnitId)")
    MoPubRewardedAds.showRewardedAd(adUnitId)
  }

  override fun onRewardedAdShowError(adUnitId: String, errorCode: MoPubErrorCode) {
    log("onRewardedAdShowError($adUnitId, $errorCode)")
  }

  override fun onRewardedAdStarted(adUnitId: String) {
    log("onRewardedAdStarted($adUnitId)")
  }

  private fun log(str: String) {
    Log.d("MoPubRewardedAd", str)
  }

  private fun MoPubReward.toFormattedString(): String {
    return "MoPubReward(isSuccessful=$isSuccessful, label=$label, amount=$amount)"
  }
}
