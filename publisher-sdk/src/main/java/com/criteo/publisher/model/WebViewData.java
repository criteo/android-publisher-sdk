package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.WebViewLoadStatus;
import com.criteo.publisher.tasks.WebViewDataTask;
import java.util.concurrent.Executor;

public class WebViewData {
    private String content;
    private WebViewLoadStatus webViewLoadStatus;
    private final Config config;

    public WebViewData(@NonNull Config config) {
        this.content = "";
        this.webViewLoadStatus = WebViewLoadStatus.NONE;
        this.config = config;
    }

    public boolean isLoaded() {
        return (webViewLoadStatus == WebViewLoadStatus.LOADED);
    }

    public void setContent(String data) {
        String dataWithTag = "";

        if (!TextUtils.isEmpty(data)) {
            dataWithTag = config.getAdTagDataMode();
            dataWithTag = dataWithTag.replace(config.getAdTagDataMacro(), data);
        }

        this.content = dataWithTag;
    }

    public boolean isLoading() {
        return webViewLoadStatus == WebViewLoadStatus.LOADING;
    }

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
        new WebViewDataTask(this, deviceInfo, criteoInterstitialAdDisplayListener).executeOnExecutor(threadPoolExecutor, displayUrl);
    }
}
