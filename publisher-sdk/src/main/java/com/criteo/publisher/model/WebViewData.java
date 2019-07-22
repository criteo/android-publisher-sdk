package com.criteo.publisher.model;

import android.text.TextUtils;
import com.criteo.publisher.Util.WebViewLoadStatus;

public class WebViewData {

    private String content;
    private WebViewLoadStatus webViewLoadStatus;

    public WebViewData() {
        this.content = "";
        this.webViewLoadStatus = WebViewLoadStatus.NONE;
    }

    public boolean isLoaded() {
        return (webViewLoadStatus == WebViewLoadStatus.LOADED);
    }

    public void setContent(String data) {
        String dataWithTag = "";

        if (!TextUtils.isEmpty(data)) {
            dataWithTag = Config.getAdTagDataMode();
            dataWithTag = dataWithTag.replace(Config.getAdTagDataMacro(), data);
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
