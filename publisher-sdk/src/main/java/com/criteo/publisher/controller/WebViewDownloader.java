package com.criteo.publisher.controller;

import android.os.AsyncTask;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.tasks.WebViewDataTask;
import com.criteo.publisher.model.WebViewData;
import java.util.concurrent.Executor;

public class WebViewDownloader {

    private WebViewData webViewData;

    public WebViewDownloader(WebViewData webviewData) {
        this.webViewData = webviewData;
    }

    public boolean isLoading() {
        return webViewData.isLoading();
    }

    public void fillWebViewHtmlContent(String displayUrl, String webViewUserAgent ,CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener) {
        Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
        new WebViewDataTask(webViewData,criteoInterstitialAdDisplayListener ).executeOnExecutor(threadPoolExecutor, displayUrl, webViewUserAgent);
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
