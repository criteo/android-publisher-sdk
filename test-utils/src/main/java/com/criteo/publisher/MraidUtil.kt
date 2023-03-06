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

import android.webkit.WebView
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.util.CompletableFuture

fun WebView.loadMraidHtml(html: String) {
  ThreadingUtil.runOnMainThreadAndWait {
    loadDataWithBaseURL(
        "https://www.criteo.com",
        html,
        "text/html",
        "UTF-8",
        ""
    )
  }
}

fun WebView.callMraidObjectBlocking(code: String = ""): String {
  val codeToAppend = if (code.isEmpty()) "" else ".$code"
  return getJavascriptResultBlocking("window.mraid$codeToAppend")
}

fun WebView.getJavascriptResultBlocking(code: String): String {
  val result = CompletableFuture<String>()
  ThreadingUtil.runOnMainThreadAndWait {
    evaluateJavascript(
        "javascript:$code",
        result::complete
    )
  }
  return result.get()
}
