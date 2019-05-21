package com.criteo.publisher.model;

import static com.criteo.publisher.model.Config.WEBVIEW_DATA_MACRO;

import android.text.TextUtils;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;

public class WebViewData {

    private String content;
    private Boolean loaded;

    public WebViewData(String content, Boolean loaded) {
        this.content = content;
        this.loaded = loaded;
    }

    public WebViewData() {
        this.content = "";
        this.loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setContent(String data, CriteoInterstitialAdListener criteoInterstitialAdListener) {
        String dataWithTag = "";

        if (!TextUtils.isEmpty(data)) {
            dataWithTag = Config.MEDIATION_AD_TAG_DATA;
            dataWithTag = dataWithTag.replace(WEBVIEW_DATA_MACRO, data);

        }

        this.loaded = !TextUtils.isEmpty(data);
        this.content = dataWithTag;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getContent() {
        return content;
    }

    public void refresh() {
        loaded = false;
        content = "";
    }
}
