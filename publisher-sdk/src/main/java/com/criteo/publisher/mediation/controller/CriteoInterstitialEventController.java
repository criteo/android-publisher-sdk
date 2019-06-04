package com.criteo.publisher.mediation.controller;

import android.text.TextUtils;
import android.webkit.URLUtil;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.mediation.tasks.CriteoInterstitialListenerCallTask;
import com.criteo.publisher.model.Slot;


public class CriteoInterstitialEventController {

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialListenerCallTask criteoInterstitialListenerCallTask;

    private WebViewDownloader webViewDownloader;

    public CriteoInterstitialEventController(
            CriteoInterstitialAdListener listener, WebViewDownloader webViewDownloader) {
        this.criteoInterstitialAdListener = listener;
        this.webViewDownloader = webViewDownloader;
    }

    public boolean isAdLoaded() {
        return webViewDownloader.getWebViewData().isLoaded();
    }

    public void fetchAdAsync(AdUnit adUnit) {

        Slot slot = Criteo.getInstance().getBidForAdUnit(adUnit);

        criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
        criteoInterstitialListenerCallTask.execute(slot);

        if (slot != null) {
            //gets Webview data from Criteo before showing Interstitialview Activity
            getWebviewDataAsync(slot.getDisplayUrl(), criteoInterstitialAdListener);
        }
    }

    protected void getWebviewDataAsync(String displayUrl, CriteoInterstitialAdListener listener) {
        if (TextUtils.isEmpty(displayUrl) || (!URLUtil.isValidUrl(String.valueOf(displayUrl)))) {
            Slot slot = null;
            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.execute(slot);
            return;
        }

        webViewDownloader.fillWebViewHtmlContent(displayUrl, listener);

    }

    public String getWebViewDataContent() {
        return webViewDownloader.getWebViewData().getContent();
    }
}
