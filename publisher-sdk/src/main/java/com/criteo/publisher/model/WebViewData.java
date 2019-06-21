package com.criteo.publisher.model;

import static com.criteo.publisher.model.Config.WEBVIEW_DATA_MACRO;

import android.text.TextUtils;
import com.criteo.publisher.Util.WebViewLoadStatus;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;

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

    public void setContent(String data, CriteoInterstitialAdListener criteoInterstitialAdListener) {
        String dataWithTag = "";

        if (!TextUtils.isEmpty(data)) {
            dataWithTag = Config.MEDIATION_AD_TAG_DATA;
            dataWithTag = dataWithTag.replace(WEBVIEW_DATA_MACRO, data);
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
