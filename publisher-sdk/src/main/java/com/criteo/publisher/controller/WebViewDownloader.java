package com.criteo.publisher.controller;

import android.support.annotation.NonNull;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.tasks.WebViewDataTask;
import java.util.concurrent.Executor;

public class WebViewDownloader {

    private WebViewData webViewData;

    public WebViewDownloader(WebViewData webviewData) {
        this.webViewData = webviewData;
    }

    public boolean isLoading() {
        return webViewData.isLoading();
    }

    public void fillWebViewHtmlContent(
        String displayUrl,
        @NonNull DeviceInfo deviceInfo,
        CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener) {
        Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
        new WebViewDataTask(webViewData, deviceInfo, criteoInterstitialAdDisplayListener).executeOnExecutor(threadPoolExecutor, displayUrl);
    }

    public WebViewData getWebViewData() {
        return webViewData;
    }

    public void refresh() {
        webViewData.refresh();
    }

    public void downloadFailed() {
        webViewData.downloadFailed();
    }

    public void loading() {
        webViewData.downloadloading();
    }
}
