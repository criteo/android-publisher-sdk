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

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.logging.LoggerFactory
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.util.PreconditionsUtil

@OpenForTesting
@Keep
class CriteoBannerView : FrameLayout {

  private val logger = LoggerFactory.getLogger(javaClass)

  @VisibleForTesting
  final lateinit var adWebView: CriteoBannerAdWebView
  private set

  @JvmField
  final var bannerAdUnit: BannerAdUnit? = null

  /**
   * Used when setting [CriteoBannerAdWebView] in XML
   */
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    val a = context.theme.obtainStyledAttributes(
        attrs,
        R.styleable.CriteoBannerView,
        0,
        0
    )
    try {
      val width = a.getInteger(
          R.styleable.CriteoBannerView_criteoAdUnitWidth,
          UNSET_DIMENSION_VALUE
      )
      val height = a.getInteger(
          R.styleable.CriteoBannerView_criteoAdUnitHeight,
          UNSET_DIMENSION_VALUE
      )
      val adUnitId = a.getString(R.styleable.CriteoBannerView_criteoAdUnitId)
      if (adUnitId != null && width != UNSET_DIMENSION_VALUE && height != UNSET_DIMENSION_VALUE) {
        bannerAdUnit = BannerAdUnit(adUnitId, AdSize(width, height))
      } else if (adUnitId == null && width == UNSET_DIMENSION_VALUE && height == UNSET_DIMENSION_VALUE) {
        bannerAdUnit = null
      } else {
        bannerAdUnit = null
        PreconditionsUtil.throwOrLog(
            IllegalStateException(
                "CriteoBannerView was not properly inflated. For InHouse integration, no attribute must " +
                    "be set. For Standalone integration, all of: criteoAdUnitId, criteoAdUnitWidth and " +
                    "criteoAdUnitHeight must be set."
            )
        )
      }
    } finally {
      a.recycle()
    }
    createAndAddAdWebView(context, attrs, bannerAdUnit, null)
    logger.log(BannerLogMessage.onBannerViewInitialized(bannerAdUnit))
  }

  /**
   * Used by server side bidding and in-house auction
   */
  constructor(context: Context) : this(context, null, null)

  /**
   * Used by Standalone
   */
  constructor(context: Context, bannerAdUnit: BannerAdUnit) : this(context, bannerAdUnit, null)

  @VisibleForTesting
  internal constructor(
      context: Context,
      bannerAdUnit: BannerAdUnit?,
      criteo: Criteo?
  ) : super(context) {
    createAndAddAdWebView(context, null, bannerAdUnit, criteo)
    logger.log(BannerLogMessage.onBannerViewInitialized(bannerAdUnit))
  }

  fun setCriteoBannerAdListener(criteoBannerAdListener: CriteoBannerAdListener?) {
    this.adWebView.setCriteoBannerAdListener(criteoBannerAdListener)
  }

  fun getCriteoBannerAdListener(): CriteoBannerAdListener? {
    return this.adWebView.getCriteoBannerAdListener()
  }

  @JvmOverloads
  fun loadAd(contextData: ContextData = ContextData()) {
    this.adWebView.loadAd(contextData)
  }

  fun loadAdWithDisplayData(displayData: String) {
    this.adWebView.loadAdWithDisplayData(displayData)
  }

  fun loadAd(bid: Bid?) {
    this.adWebView.loadAd(bid)
  }

  fun destroy() {
    adWebView.destroy()
    removeAllViews()
  }

  private fun createAndAddAdWebView(
      context: Context,
      attrs: AttributeSet?,
      bannerAdUnit: BannerAdUnit?,
      criteo: Criteo?
  ) {
    this.bannerAdUnit = bannerAdUnit
    adWebView = DependencyProvider.getInstance()
        .provideAdWebViewFactory()
        .create(context, attrs, bannerAdUnit, criteo, this)
    this.addView(
        adWebView, LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT
    )
    )
  }

  companion object {
    private const val UNSET_DIMENSION_VALUE = -1
  }
}
