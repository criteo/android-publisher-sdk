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

    private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

    private CriteoInterstitialListenerCallTask criteoInterstitialListenerCallTask;

    private WebViewDownloader webViewDownloader;

    private DeviceInfo deviceInfo;

    private final Criteo criteo;

    public CriteoInterstitialEventController(
            CriteoInterstitialAdListener listener, CriteoInterstitialAdDisplayListener adDisplayListener,
            WebViewDownloader webViewDownloader,
            Criteo criteo) {
        this.criteoInterstitialAdListener = listener;
        this.criteoInterstitialAdDisplayListener = adDisplayListener;
        this.webViewDownloader = webViewDownloader;
        this.deviceInfo = criteo.getDeviceInfo();
        this.criteo = criteo;
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
                slot = criteo.getBidForAdUnit(adUnit);
            }

            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(
                    criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.execute(slot);

            if (slot != null && slot.isValid() && URLUtil.isValidUrl(slot.getDisplayUrl())) {
                //gets Webview data from Criteo before showing Interstitialview Activity
                getWebviewDataAsync(slot.getDisplayUrl());
            } else {
                webViewDownloader.downloadFailed();
            }
        }
    }

    private void getWebviewDataAsync(String displayUrl) {
        if (TextUtils.isEmpty(displayUrl) || (!URLUtil.isValidUrl(displayUrl))) {
            Slot slot = null;
            webViewDownloader.downloadFailed();
            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, slot);
            return;
        }

        webViewDownloader
                .fillWebViewHtmlContent(displayUrl, deviceInfo.getUserAgent(), criteoInterstitialAdDisplayListener);

    }

    public String getWebViewDataContent() {
        return webViewDownloader.getWebViewData().getContent();
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

        criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
        criteoInterstitialListenerCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tokenValue);

        if (tokenValue != null) {
            getWebviewDataAsync(tokenValue.getDisplayUrl());
        }
    }
}
