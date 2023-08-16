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

import android.content.res.Configuration
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient
import com.criteo.publisher.advancednative.VisibilityListener
import com.criteo.publisher.advancednative.VisibilityTracker
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.logging.LoggerFactory
import com.criteo.publisher.util.DeviceUtil
import java.io.IOException

@OpenForTesting
@Suppress("TooManyFunctions")
internal abstract class CriteoMraidController(
    private val adWebView: AdWebView,
    private val visibilityTracker: VisibilityTracker,
    private val mraidInteractor: MraidInteractor,
    private val mraidMessageHandler: MraidMessageHandler,
    private val deviceUtil: DeviceUtil
) : MraidController, VisibilityListener, MraidMessageHandlerListener, AdWebViewClientListener {

  private var isViewable: Boolean? = null
  private var adWebViewClient: AdWebViewClient? = null
  private var mraidState: MraidState = MraidState.LOADING
  private var isMraidAd = false

  override val currentState: MraidState
    get() = mraidState

  protected val logger = LoggerFactory.getLogger(javaClass)

  init {
    setupMessageHandler()
  }

  override fun onVisible() {
    reportViewabilityIfNeeded(true)
  }

  override fun onGone() {
    reportViewabilityIfNeeded(false)
  }

  override fun onOpen(url: String) {
    adWebViewClient?.open(url)
  }

  override fun onExpand(width: Double, height: Double) {
    doExpand(width, height, onResult = {
      when (it) {
        is MraidActionResult.Error -> mraidInteractor.notifyError(it.message, it.action)
        MraidActionResult.Success -> {
          mraidInteractor.notifyExpanded()
          mraidState = MraidState.EXPANDED
        }
      }
    })
  }

  override fun onClose() {
    doClose(onResult = {
      when (it) {
        is MraidActionResult.Error -> mraidInteractor.notifyError(it.message, it.action)
        MraidActionResult.Success -> setAsClosed()
      }
    })
  }

  override fun onPageFinished() {
    invokeIfMraidAd {
      onMraidLoaded()
    }
  }

  override fun onOpenFailed() {
    invokeIfMraidAd {
      mraidInteractor.notifyError("Error during url open", "open")
    }
  }

  override fun shouldInterceptRequest(url: String): WebResourceResponse? {
    return if (url.endsWith(MRAID_SCRIPT_NAME)) {
      try {
        val stream = adWebView.context.assets.open(MRAID_FILENAME)

        isMraidAd = true
        WebResourceResponse("text/javascript", "UTF-8", stream)
      } catch (e: IOException) {
        logger.log(MraidLogMessage.onErrorDuringMraidFileInject(e))
        null
      }
    } else {
      null
    }
  }

  override fun onWebViewClientSet(client: WebViewClient) {
    (client as? AdWebViewClient)?.let {
      adWebViewClient = it
      it.setAdWebViewClientListener(this)
    }
  }

  override fun onConfigurationChange(newConfig: Configuration?) {
    invokeIfMraidAd {
      newConfig?.let {
        setMaxSize(it)
        setScreenSize()
      }
    }
  }

  override fun onClosed() {
    invokeIfMraidAd {
      setAsClosed()
    }
  }

  private fun onMraidLoaded() {
    visibilityTracker.watch(adWebView, this)
    setMaxSize(adWebView.resources.configuration)
    setScreenSize()
    mraidState = MraidState.DEFAULT
    mraidInteractor.notifyReady(getPlacementType())
  }

  private fun reportViewabilityIfNeeded(isVisible: Boolean) {
    if (isViewable != isVisible) {
      isViewable = isVisible
      isViewable?.let {
        mraidInteractor.setIsViewable(it)
      }
    }
  }

  private fun setupMessageHandler() {
    adWebView.addJavascriptInterface(mraidMessageHandler, WEB_VIEW_INTERFACE_NAME)
    mraidMessageHandler.setListener(this)
  }

  private fun invokeIfMraidAd(action: () -> Unit) {
    if (isMraidAd) {
      action.invoke()
    }
  }

  private fun setMaxSize(configuration: Configuration) {
    mraidInteractor.setMaxSize(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        adWebView.resources.displayMetrics.density.toDouble()
    )
  }

  private fun setScreenSize() {
    val screenSize = deviceUtil.getRealSceeenSize()
    mraidInteractor.setScreenSize(screenSize.width, screenSize.height)
  }

  private fun updateCurrentStateOnClose() {
    mraidState = when (currentState) {
      MraidState.EXPANDED -> MraidState.DEFAULT
      MraidState.DEFAULT -> MraidState.HIDDEN
      else -> currentState
    }
  }

  private fun setAsClosed() {
    if (currentState == MraidState.DEFAULT || currentState == MraidState.EXPANDED) {
      mraidInteractor.notifyClosed()
    }
    updateCurrentStateOnClose()
  }

  companion object {
    const val WEB_VIEW_INTERFACE_NAME = "criteoMraidBridge"
    const val MRAID_SCRIPT_NAME = "mraid.js"
    const val MRAID_FILENAME = "criteo-mraid.js"
  }
}
