package com.criteo.publisher;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.URLUtil;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.controller.WebViewDownloader;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.tasks.CriteoInterstitialListenerCallTask;


public class CriteoInterstitialEventController {

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialListenerCallTask criteoInterstitialListenerCallTask;

    private WebViewDownloader webViewDownloader;

    private DeviceInfo deviceInfo;

    public CriteoInterstitialEventController(
            CriteoInterstitialAdListener listener, WebViewDownloader webViewDownloader) {
        this.criteoInterstitialAdListener = listener;
        this.webViewDownloader = webViewDownloader;
        this.deviceInfo = Criteo.getInstance().getDeviceInfo();

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

            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(
                    criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.execute(slot);

            if (slot != null && slot.isValid() && URLUtil.isValidUrl(String.valueOf(slot.getDisplayUrl()))) {
                //gets Webview data from Criteo before showing Interstitialview Activity
                getWebviewDataAsync(slot.getDisplayUrl());
            }
        }
    }

    protected void getWebviewDataAsync(String displayUrl) {
        if (TextUtils.isEmpty(displayUrl) || (!URLUtil.isValidUrl(String.valueOf(displayUrl)))) {
            Slot slot = null;
            webViewDownloader.downloadFailed();
            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, slot);
            return;
        }

        webViewDownloader.fillWebViewHtmlContent(displayUrl, deviceInfo.getWebViewUserAgent());

    }

    public String getWebViewDataContent() {
        return webViewDownloader.getWebViewData().getContent();
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = Criteo.getInstance().getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

        criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
        criteoInterstitialListenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tokenValue);

        if (tokenValue != null) {
            getWebviewDataAsync(tokenValue.getDisplayUrl());
        }
    }
}
