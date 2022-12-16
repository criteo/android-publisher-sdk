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

package com.criteo.publisher.tasks;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.model.Config;
import java.lang.ref.Reference;

public class CriteoBannerLoadTask extends SafeRunnable {

  @NonNull
  private final Reference<? extends WebView> webViewRef;

  @NonNull
  private final Config config;

  @NonNull
  private final WebViewClient webViewClient;

  @NonNull
  private final String displayUrl;

  /**
   * Taking WebViewClient as a constructor as all WebView/CriteoBannerView methods must be called on
   * the same UI thread. WebView.getSettings().setJavaScriptEnabled() & WebView.setWebViewClient()
   * throws if not done in the onPostExecute() as onPostExecute runs on the UI thread
   */
  public CriteoBannerLoadTask(
      @NonNull Reference<? extends WebView> webViewRef,
      @NonNull WebViewClient webViewClient,
      @NonNull Config config,
      @NonNull String displayUrl) {
    this.webViewRef = webViewRef;
    this.webViewClient = webViewClient;
    this.config = config;
    this.displayUrl = displayUrl;
  }

  @Override
  public void runSafely() {
    loadWebview();
  }

  private void loadWebview() {
    WebView webView = webViewRef.get();
    if (webView != null) {
      String finalDisplayUrl = computeFinalDisplayUrl();

      webView.getSettings().setJavaScriptEnabled(true);
      webView.setWebViewClient(this.webViewClient);
      webView.loadDataWithBaseURL("https://www.criteo.com", finalDisplayUrl, "text/html", "UTF-8", "");
    }
  }

  @NonNull
  private String computeFinalDisplayUrl() {
    String displayUrlWithTag = config.getAdTagUrlMode();
    return displayUrlWithTag.replace(config.getDisplayUrlMacro(), displayUrl);
  }
}
