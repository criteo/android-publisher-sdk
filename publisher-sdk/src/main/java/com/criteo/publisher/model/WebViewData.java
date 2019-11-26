package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.criteo.publisher.Util.WebViewLoadStatus;

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

    public void downloadSucceeeded() {
        this.webViewLoadStatus = WebViewLoadStatus.LOADED;
    }

    public void downloadloading() {
        this.webViewLoadStatus = WebViewLoadStatus.LOADING;
    }
}
