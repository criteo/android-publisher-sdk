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

import android.content.ComponentName
import android.webkit.WebView
import com.criteo.publisher.util.CompletableFuture

internal class TestAdWebViewClient(listener: RedirectionListener, hostActivityName: ComponentName?) :
    AdWebViewClient(listener, hostActivityName) {

  private val pageFinishedLoading = CompletableFuture<Unit>()

  override fun onPageFinished(view: WebView?, url: String?) {
    super.onPageFinished(view, url)
    pageFinishedLoading.complete(Unit)
  }

  fun waitForPageToFinishLoading() {
    pageFinishedLoading.get()
  }
}
