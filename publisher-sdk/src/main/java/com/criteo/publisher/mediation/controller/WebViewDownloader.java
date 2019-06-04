package com.criteo.publisher.mediation.controller;

import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.tasks.WebViewDataTask;
import com.criteo.publisher.model.WebViewData;

public class WebViewDownloader {

    private WebViewData webViewData;

    public WebViewDownloader(WebViewData webviewData) {
        this.webViewData = webviewData;

    }


    public void fillWebViewHtmlContent(String displayUrl, CriteoInterstitialAdListener listener) {

        webViewData.refresh();
        new WebViewDataTask(webViewData, listener).execute(displayUrl);
    }

    public WebViewData getWebViewData() {
        return webViewData;
    }
}
