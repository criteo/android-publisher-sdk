package com.criteo.publisher.model;

import android.text.TextUtils;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;

public class WebviewData {

    private static final String WEBVIEW_DATA_MACRO = "%%webviewData%%";

    private String content;
    private Boolean loaded;

    public WebviewData(String content, Boolean loaded) {
        this.content = content;
        this.loaded = loaded;
    }

    public WebviewData() {
        this.content = "";
        this.loaded = false;
    }


    public boolean isLoaded() {
        return loaded;
    }

    public void setContent(String data) {
        String dataWithTag = "";

        if (!TextUtils.isEmpty(data)) {
            dataWithTag = Config.mediationAdTagData;
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
