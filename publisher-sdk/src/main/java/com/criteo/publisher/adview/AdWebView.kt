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

internal open class AdWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs), AdWebViewClientListener {

  private val mraidInteractor by lazy {
    DependencyProvider.getInstance().provideMraidInteractor(
        this
    )
  }

  override fun setWebViewClient(client: WebViewClient) {
    (client as? AdWebViewClient)?.setAdWebViewClientListener(this)
    super.setWebViewClient(client)
  }

  override fun onPageFinished() {
    mraidInteractor.notifyReady()
  }
}
