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

package com.criteo.publisher.view;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;

import android.os.Build.VERSION_CODES;
import android.webkit.WebView;
import android.webkit.WebView.VisualStateCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.util.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class WebViewClicker {

  public String getAdHtmlWithClickUrl(@NonNull String clickUrl) {
    return "<html><body><a href='" + clickUrl + "'>My Awesome Ad</a></body></html>";
  }

  @RequiresApi(api = VERSION_CODES.O)
  public void loadHtmlAndSimulateClickOnAd(@NonNull WebView webView, @NonNull String clickUrl) throws Exception {
    String html = getAdHtmlWithClickUrl(clickUrl);

    runOnMainThreadAndWait(() -> {
      webView.loadData(html, "text/html", "UTF-8");
    });

    simulateClickOnAd(webView);
  }

  @RequiresApi(api = VERSION_CODES.M)
  public void simulateClickOnAd(@NonNull WebView webView) throws Exception {
    waitUntilWebViewIsLoaded(webView);

    CompletableFuture<Void> isClickDone = new CompletableFuture<>();

    // Simulate click via JavaScript
    runOnMainThreadAndWait(() -> {
      webView.evaluateJavascript("(function() {\n"
          + "  var elements = document.getElementsByTagName('a');\n"
          + "  if (elements.length != 1) {\n"
          + "    return false;\n"
          + "  }\n"
          + "  elements[0].click();\n"
          + "  return true;\n"
          + "})();", value -> {
        if (!"true".equals(value)) {
          isClickDone.completeExceptionally(new IllegalStateException("Clickable element was not found in the WebView"));
        } else {
          isClickDone.complete(null);
        }
      });
    });

    isClickDone.get();
  }

  @RequiresApi(api = VERSION_CODES.M)
  public void waitUntilWebViewIsLoaded(@NonNull WebView webView) throws Exception {
    CountDownLatch isHtmlLoaded = new CountDownLatch(1);

    runOnMainThreadAndWait(() -> {
      webView.postVisualStateCallback(42, new VisualStateCallback() {
        @Override
        public void onComplete(long ignored) {
          isHtmlLoaded.countDown();
        }
      });
    });

    isHtmlLoaded.await();
  }

}
