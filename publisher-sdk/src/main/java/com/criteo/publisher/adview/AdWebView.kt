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
import android.content.res.Configuration
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient

abstract class AdWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

  var mraidController: MraidController = DummyMraidController()
  private set

  abstract fun provideMraidController(): MraidController

  override fun setWebViewClient(client: WebViewClient) {
    mraidController.resetToDefault()
    // create new mraid controller since new ad is loaded
    mraidController = provideMraidController()
    mraidController.onWebViewClientSet(client)
    super.setWebViewClient(client)
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    super.onConfigurationChanged(newConfig)
    mraidController.onConfigurationChange(newConfig)
  }
}
