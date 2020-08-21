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

package com.criteo.publisher.model;

import androidx.annotation.NonNull;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import com.criteo.publisher.tasks.WebViewDataTask;
import com.criteo.publisher.util.WebViewLoadStatus;
import java.util.concurrent.Executor;

public class WebViewData {

  @NonNull
  private String content;

  @NonNull
  private WebViewLoadStatus webViewLoadStatus;

  @NonNull
  private final Config config;

  @NonNull
  private final PubSdkApi api;

  public WebViewData(@NonNull Config config, @NonNull PubSdkApi api) {
    this.content = "";
    this.webViewLoadStatus = WebViewLoadStatus.NONE;
    this.config = config;
    this.api = api;
  }

  public boolean isLoaded() {
    return webViewLoadStatus == WebViewLoadStatus.LOADED;
  }

  public void setContent(@NonNull String data) {
    String dataWithTag = config.getAdTagDataMode();
    content = dataWithTag.replace(config.getAdTagDataMacro(), data);
  }

  public boolean isLoading() {
    return webViewLoadStatus == WebViewLoadStatus.LOADING;
  }

  @NonNull
  public String getContent() {
    return content;
  }

  public void refresh() {
    webViewLoadStatus = WebViewLoadStatus.NONE;
    content = "";
  }

  public void downloadFailed() {
    this.webViewLoadStatus = WebViewLoadStatus.FAILED;
  }

  public void downloadSucceeded() {
    this.webViewLoadStatus = WebViewLoadStatus.LOADED;
  }

  public void downloadLoading() {
    this.webViewLoadStatus = WebViewLoadStatus.LOADING;
  }

  public void fillWebViewHtmlContent(
      @NonNull String displayUrl,
      @NonNull DeviceInfo deviceInfo,
      @NonNull InterstitialListenerNotifier listenerNotifier
  ) {
    Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();

    Runnable task = new WebViewDataTask(
        displayUrl,
        this,
        deviceInfo,
        listenerNotifier,
        api
    );

    threadPoolExecutor.execute(task);
  }
}
