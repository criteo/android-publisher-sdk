package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.util.WebViewLoadStatus;
import com.criteo.publisher.tasks.WebViewDataTask;
import java.util.concurrent.Executor;

public class WebViewData {

  @NonNull
  private String content;

  @NonNull
  private WebViewLoadStatus webViewLoadStatus;

  @NonNull
  private final Config config;

  public WebViewData(@NonNull Config config) {
    this.content = "";
    this.webViewLoadStatus = WebViewLoadStatus.NONE;
    this.config = config;
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
      @Nullable CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener) {
    Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
    new WebViewDataTask(this, deviceInfo, criteoInterstitialAdDisplayListener)
        .executeOnExecutor(threadPoolExecutor, displayUrl);
  }
}
