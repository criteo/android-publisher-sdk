package com.criteo.publisher;

import android.text.TextUtils;
import android.webkit.URLUtil;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.controller.WebViewDownloader;
import com.criteo.publisher.mediation.tasks.CriteoInterstitialListenerCallTask;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;


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

    public void refresh() {
        webViewDownloader.refresh();
    }

    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = null;
        if (!webViewDownloader.isLoading()) {
            webViewDownloader.loading();

            if (adUnit != null) {
                slot = Criteo.getInstance().getBidForAdUnit(adUnit);
            }

            if (slot != null && slot.isValid()) {
                //gets Webview data from Criteo before showing Interstitialview Activity
                getWebviewDataAsync(slot.getDisplayUrl(), criteoInterstitialAdListener);
            } else {
                criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(
                        criteoInterstitialAdListener);
                criteoInterstitialListenerCallTask.execute(slot);
            }
        }
    }

    protected void getWebviewDataAsync(String displayUrl, CriteoInterstitialAdListener listener) {
        if (TextUtils.isEmpty(displayUrl) || (!URLUtil.isValidUrl(String.valueOf(displayUrl)))) {
            Slot slot = null;
            webViewDownloader.downloadFailed();
            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.execute(slot);
            return;
        }

        webViewDownloader.fillWebViewHtmlContent(displayUrl, listener);

    }

    public String getWebViewDataContent() {
        return webViewDownloader.getWebViewData().getContent();
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = Criteo.getInstance().getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

        criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
        criteoInterstitialListenerCallTask.execute(tokenValue);

        if (tokenValue != null) {
            getWebviewDataAsync(tokenValue.getDisplayUrl(), criteoInterstitialAdListener);
        }
    }
}
