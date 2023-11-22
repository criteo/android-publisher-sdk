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

package com.criteo.publisher.adview

import android.view.View

/**
 * Class used to pass banner from banner container
 * While banner is expanded we keep reference here, so we can check
 * if we already have expanded banner and not expand another one
 */
@Suppress("TooManyFunctions")
internal class MraidExpandBannerMediator {

  private var expandedBannerView: View? = null
  private var bannerListener: MraidExpandBannerListener? = null
  private var expandedActivityListener: MraidExpandedActivityListener? = null

  fun hasAnyExpandedBanner() = expandedBannerView != null

  fun saveForExpandedActivity(bannerView: View) {
    expandedBannerView = bannerView
  }

  fun getExpandedBannerView() = expandedBannerView

  fun clearExpandedBannerView() {
    expandedBannerView = null
  }

  fun setBannerListener(
      listener: MraidExpandBannerListener
  ) {
    this.bannerListener = listener
  }

  fun setExpandedActivityListener(listener: MraidExpandedActivityListener) {
    this.expandedActivityListener = listener
  }

  fun removeBannerListener() {
    bannerListener = null
  }

  fun removeExpandedActivityListener() {
    expandedActivityListener = null
  }

  fun requestClose() {
    bannerListener?.onCloseRequested()
  }

  fun requestOrientationChange(allowOrientationChange: Boolean, orientation: MraidOrientation) {
    bannerListener?.onOrientationRequested(allowOrientationChange, orientation)
  }

  fun notifyOnBackClicked() {
    expandedActivityListener?.onBackClicked()
  }
}

interface MraidExpandBannerListener {
  fun onOrientationRequested(allowOrientationChange: Boolean, orientation: MraidOrientation)
  fun onCloseRequested()
}

interface MraidExpandedActivityListener {
  fun onBackClicked()
}
