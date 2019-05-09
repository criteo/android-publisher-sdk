package com.criteo.publisher.mediation.controller;

import android.text.TextUtils;
import com.criteo.publisher.mediation.tasks.WebViewDataTask;
import com.criteo.publisher.model.WebviewData;

public class WebViewDownloader {

    private WebviewData webViewData;

    public WebViewDownloader(WebviewData webviewData) {
        this.webViewData = webviewData;

    }


    public void fillWebViewHtmlContent(String displayUrl) {
        if (!TextUtils.isEmpty(webViewData.getContent())) {
            webViewData.setLoaded(true);
            return;
        }

        webViewData.refresh();
        new WebViewDataTask(webViewData).execute(displayUrl);
    }

    public WebviewData getWebViewData() {
        return webViewData;
    }
}
