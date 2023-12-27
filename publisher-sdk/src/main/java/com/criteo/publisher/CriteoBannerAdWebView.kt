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
import com.criteo.publisher.BannerLogMessage.onBannerViewFailedToReloadDuringExpandedState
import com.criteo.publisher.BannerLogMessage.onBannerViewLoading
import com.criteo.publisher.ErrorLogMessage.onUncaughtErrorAtPublicApi
import com.criteo.publisher.adview.AdWebView
import com.criteo.publisher.adview.MraidController
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.logging.LoggerFactory
import com.criteo.publisher.model.BannerAdUnit

@Suppress("TooManyFunctions")
@OpenForTesting
class CriteoBannerAdWebView(
    context: Context,
    attrs: AttributeSet?,
    val bannerAdUnit: BannerAdUnit?,
    criteo: Criteo?,
    val parentContainer: CriteoBannerView
) : AdWebView(context, attrs) {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Null means that the singleton Criteo should be used.
   *
   *
   * [Criteo.getInstance] is fetched lazily so publishers may call the constructor without
   * having to init the SDK before.
   */
  private var criteo: Criteo? = null
  var adListener: CriteoBannerAdListener? = null

  private val eventController: CriteoBannerEventController by lazy {
    getCriteo().createBannerController(this)
  }

  override fun provideMraidController(): MraidController {
    return DependencyProvider.getInstance().provideMraidController(MraidPlacementType.INLINE, this)
  }

  init {
    this.criteo = criteo
  }

  fun setCriteoBannerAdListener(criteoBannerAdListener: CriteoBannerAdListener?) {
    this.adListener = criteoBannerAdListener
  }

  fun getCriteoBannerAdListener(): CriteoBannerAdListener? {
    return adListener
  }

  @JvmOverloads
  @Suppress("TooGenericExceptionCaught")
  fun loadAd(contextData: ContextData = ContextData()) {
    try {
      doLoadAd(contextData)
    } catch (tr: Throwable) {
      logger.log(onUncaughtErrorAtPublicApi(tr))
    }
  }

  fun loadAdWithDisplayData(displayData: String) {
    loadIfAdNotExpanded {
      eventController.notifyFor(CriteoListenerCode.VALID)
      eventController.displayAd(displayData)
    }
  }

  private fun doLoadAd(contextData: ContextData) {
    loadIfAdNotExpanded {
      logger.log(onBannerViewLoading(parentContainer))
      integrationRegistry.declare(Integration.STANDALONE)
      eventController.fetchAdAsync(bannerAdUnit, contextData)
    }
  }

  @Suppress("TooGenericExceptionCaught")
  fun loadAd(bid: Bid?) {
    try {
      doLoadAd(bid)
    } catch (tr: Throwable) {
      logger.log(onUncaughtErrorAtPublicApi(tr))
    }
  }

  private fun doLoadAd(bid: Bid?) {
    loadIfAdNotExpanded {
      logger.log(onBannerViewLoading(parentContainer, bid))
      integrationRegistry.declare(Integration.IN_HOUSE)
      eventController.fetchAdAsync(bid)
    }
  }

  private fun loadIfAdNotExpanded(loadAction: () -> Unit) {
    if (mraidController.currentState == MraidState.EXPANDED) {
      logger.log(onBannerViewFailedToReloadDuringExpandedState())
    } else {
      loadAction()
    }
  }

  private fun getCriteo(): Criteo {
    return criteo ?: Criteo.getInstance()
  }

  override fun destroy() {
    // We do not destroy webView in expanded state since mraid ad
    // might request orientation change. If rotation is performed
    // original container will be destroyed and webView will not
    // return to original container. WebView in expanded state
    // survives orientation change and will be kept until it is closed
    if (mraidController.currentState != MraidState.EXPANDED) {
      super.destroy()
    }
  }

  private val integrationRegistry: IntegrationRegistry
    get() = DependencyProvider.getInstance().provideIntegrationRegistry()
}
