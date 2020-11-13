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

package com.criteo.publisher.interstitial

import com.criteo.publisher.Bid
import com.criteo.publisher.CriteoInterstitial
import com.criteo.publisher.adUnit
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.loggingId
import com.criteo.publisher.model.InterstitialAdUnit

internal object InterstitialLogMessage {

  @JvmStatic
  fun onInterstitialInitialized(adUnit: InterstitialAdUnit?) = LogMessage(message =
    "Interstitial initialized for $adUnit"
  )

  @JvmStatic
  fun onInterstitialLoading(interstitial: CriteoInterstitial) = LogMessage(message =
    "Interstitial(${interstitial.adUnit}) is loading"
  )

  @JvmStatic
  fun onInterstitialLoading(interstitial: CriteoInterstitial, bid: Bid?) = LogMessage(message =
    "Interstitial(${interstitial.adUnit}) is loading with bid ${bid?.loggingId}"
  )

  @JvmStatic
  fun onInterstitialLoaded(interstitial: CriteoInterstitial?) = LogMessage(message =
    "Interstitial(${interstitial?.adUnit}) is loaded"
  )

  @JvmStatic
  fun onInterstitialFailedToLoad(interstitial: CriteoInterstitial?) = LogMessage(message =
    "Interstitial(${interstitial?.adUnit}) failed to load"
  )

  @JvmStatic
  fun onCheckingIfInterstitialIsLoaded(interstitial: CriteoInterstitial, isAdLoaded: Boolean) = LogMessage(message =
  "Interstitial(${interstitial.adUnit}) is isAdLoaded=$isAdLoaded"
  )

  @JvmStatic
  fun onInterstitialShowing(interstitial: CriteoInterstitial) = LogMessage(message =
    "Interstitial(${interstitial.adUnit}) is showing"
  )
}
