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

package com.criteo.publisher

import android.util.Log
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.model.BannerAdUnit

internal object BannerLogMessage {

  @JvmStatic
  fun onBannerViewInitialized(adUnit: BannerAdUnit?) = LogMessage(message =
    "BannerView initialized for $adUnit"
  )

  @JvmStatic
  fun onBannerViewLoading(bannerView: CriteoBannerView) = LogMessage(message =
    "BannerView(${bannerView.bannerAdUnit}) is loading"
  )

  @JvmStatic
  fun onBannerViewLoading(bannerView: CriteoBannerView, bid: Bid?) = LogMessage(message =
    "BannerView(${bannerView.bannerAdUnit}) is loading with bid ${bid?.loggingId}"
  )

  @JvmStatic
  fun onBannerViewLoaded(bannerView: CriteoBannerView?) = LogMessage(message =
    "BannerView(${bannerView?.bannerAdUnit}) is loaded"
  )

  @JvmStatic
  fun onBannerViewFailedToLoad(bannerView: CriteoBannerView?) = LogMessage(message =
    "BannerView(${bannerView?.bannerAdUnit}) failed to load"
  )

  @JvmStatic
  fun onBannerViewFailedToReloadDuringExpandedState() = LogMessage(
      message = "BannerView can't be reloaded during expanded state",
      level = Log.ERROR
  )

  @JvmStatic
  fun onBannerFailedToExpand(bannerView: CriteoBannerView?, throwable: Throwable) = LogMessage(
      message = "BannerView(${bannerView?.bannerAdUnit}) failed to expand",
      level = Log.ERROR,
      throwable = throwable
  )

  @JvmStatic
  fun onBannerFailedToClose(bannerView: CriteoBannerView?, throwable: Throwable) = LogMessage(
      message = "BannerView(${bannerView?.bannerAdUnit}) failed to close",
      level = Log.ERROR,
      throwable = throwable
  )

  @JvmStatic
  fun onBannerFailedToResize(bannerView: CriteoBannerView?, throwable: Throwable) = LogMessage(
      message = "BannerView(${bannerView?.bannerAdUnit}) failed to resize",
      level = Log.ERROR,
      throwable = throwable
  )

  @JvmStatic
  fun onBannerFailedToSetOrientationProperties(bannerView: CriteoBannerView?, throwable: Throwable) = LogMessage(
      message = "BannerView(${bannerView?.bannerAdUnit}) is failed to setOrientationProperties",
      level = Log.ERROR,
      throwable = throwable
  )
}
