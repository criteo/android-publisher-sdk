package com.criteo.publisher.controller;

import android.os.AsyncTask;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.tasks.WebViewDataTask;
import com.criteo.publisher.model.WebViewData;

public class WebViewDownloader {

    private WebViewData webViewData;

    public WebViewDownloader(WebViewData webviewData) {
        this.webViewData = webviewData;
    }

    public boolean isLoading() {
        return webViewData.isLoading();
    }

    public void fillWebViewHtmlContent(String displayUrl, String webViewUserAgent ,CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener) {
        new WebViewDataTask(webViewData,criteoInterstitialAdDisplayListener ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, displayUrl, webViewUserAgent);
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
