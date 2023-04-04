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
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.BannerLogMessage.onBannerViewInitialized
import com.criteo.publisher.BannerLogMessage.onBannerViewLoading
import com.criteo.publisher.ErrorLogMessage.onUncaughtErrorAtPublicApi
import com.criteo.publisher.adview.AdWebView
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.logging.LoggerFactory
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.util.PreconditionsUtil

@Keep
class CriteoBannerView : AdWebView {

  private val logger = LoggerFactory.getLogger(javaClass)

  final val bannerAdUnit: BannerAdUnit?

  /**
  * Null means that the singleton Criteo should be used.
  *
  *
  * [Criteo.getInstance] is fetched lazily so publishers may call the constructor without
  * having to init the SDK before.
  */
  private var criteo: Criteo? = null

  var adListener: CriteoBannerAdListener? = null

  private var criteoBannerEventController: CriteoBannerEventController? = null

  /**
   * Used when setting [CriteoBannerView] in XML
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
                "CriteoBannerView was not properly inflated. For InHouse integration, no attribute must "
                    + "be set. For Standalone integration, all of: criteoAdUnitId, criteoAdUnitWidth and "
                    + "criteoAdUnitHeight must be set."
            )
        )
      }
    } finally {
      a.recycle()
    }
    logger.log(onBannerViewInitialized(bannerAdUnit))
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
    this.bannerAdUnit = bannerAdUnit
    this.criteo = criteo
    logger.log(onBannerViewInitialized(bannerAdUnit))
  }

  fun setCriteoBannerAdListener(criteoBannerAdListener: CriteoBannerAdListener?) {
    this.adListener = criteoBannerAdListener
  }

  fun getCriteoBannerAdListener(): CriteoBannerAdListener? {
    return adListener
  }

  @JvmOverloads
  fun loadAd(contextData: ContextData = ContextData()) {
    try {
      doLoadAd(contextData)
    } catch (tr: Throwable) {
      logger.log(onUncaughtErrorAtPublicApi(tr))
    }
  }

  fun loadAdWithDisplayData(displayData: String) {
    getOrCreateController().notifyFor(CriteoListenerCode.VALID)
    getOrCreateController().displayAd(displayData)
  }

  private fun doLoadAd(contextData: ContextData) {
    logger.log(onBannerViewLoading(this))
    integrationRegistry.declare(Integration.STANDALONE)
    getOrCreateController().fetchAdAsync(bannerAdUnit, contextData)
  }

  fun loadAd(bid: Bid?) {
    try {
      doLoadAd(bid)
    } catch (tr: Throwable) {
      logger.log(onUncaughtErrorAtPublicApi(tr))
    }
  }

  override fun getPlacementType(): MraidPlacementType {
    return MraidPlacementType.INLINE
  }

  private fun doLoadAd(bid: Bid?) {
    logger.log(onBannerViewLoading(this, bid))
    integrationRegistry.declare(Integration.IN_HOUSE)
    getOrCreateController().fetchAdAsync(bid)
  }

  internal fun getOrCreateController() : CriteoBannerEventController {
      if (criteoBannerEventController == null) {
        criteoBannerEventController = getCriteo().createBannerController(this)
      }
      return criteoBannerEventController!!
    }

  private fun getCriteo(): Criteo {
    return criteo ?: Criteo.getInstance()
  }

  private val integrationRegistry: IntegrationRegistry
    private get() = DependencyProvider.getInstance().provideIntegrationRegistry()

  companion object {
    private const val UNSET_DIMENSION_VALUE = -1
  }
}