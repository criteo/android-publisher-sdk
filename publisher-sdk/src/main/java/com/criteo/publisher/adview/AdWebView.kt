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

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import com.criteo.publisher.DependencyProvider
import com.criteo.publisher.advancednative.VisibilityListener

internal open class AdWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs), AdWebViewClientListener, VisibilityListener, MraidMessageHandlerListener {

  private var adWebViewClient: AdWebViewClient? = null

  private val mraidInteractor by lazy {
    DependencyProvider.getInstance().provideMraidInteractor(
        this
    )
  }
  private val visibilityTracker by lazy {
    DependencyProvider.getInstance()
        .provideVisibilityTracker()
  }

  private val mraidMessageHandler = DependencyProvider.getInstance().provideMraidMessageHandler()

  private var isViewable: Boolean? = null

  init {
    setupMessageHandler()
  }

  override fun setWebViewClient(client: WebViewClient) {
    (client as? AdWebViewClient)?.let {
      adWebViewClient = it
      it.setAdWebViewClientListener(this)
    }
    super.setWebViewClient(client)
  }

  override fun onPageFinished() {
    invokeIfMraidAd {
      visibilityTracker.watch(this, this)
      mraidInteractor.notifyReady(getPlacementType())
    }
  }

  override fun onOpenFailed() {
    invokeIfMraidAd {
      mraidInteractor.notifyError("Error during url open", "open")
    }
  }

  protected fun getPlacementType(): MraidPlacementType = MraidPlacementType.INTERSTITIAL

  override fun onVisible() {
    reportViewabilityIfNeeded(true)
  }

  override fun onGone() {
    reportViewabilityIfNeeded(false)
  }

  override fun onOpen(url: String) {
    adWebViewClient?.open(url)
  }

  private fun reportViewabilityIfNeeded(isVisible: Boolean) {
    if (isViewable != isVisible) {
      isViewable = isVisible
      isViewable?.let {
        mraidInteractor.setIsViewable(it)
      }
    }
  }

  private fun invokeIfMraidAd(action: () -> Unit) {
    adWebViewClient?.isMraidAd()
        ?.takeIf { it }
        ?.let { action.invoke() }
  }

  private fun setupMessageHandler() {
    addJavascriptInterface(mraidMessageHandler, WEB_VIEW_INTERFACE_NAME)
    mraidMessageHandler.setListener(this)
  }

  companion object {
    private const val WEB_VIEW_INTERFACE_NAME = "criteoMraidBridge"
  }
}
