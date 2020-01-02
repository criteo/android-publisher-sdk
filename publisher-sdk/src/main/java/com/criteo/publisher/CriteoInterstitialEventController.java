package com.criteo.publisher;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.controller.WebViewDownloader;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.tasks.CriteoInterstitialListenerCallTask;
import java.util.concurrent.Executor;


public class CriteoInterstitialEventController {

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

    private CriteoInterstitialListenerCallTask criteoInterstitialListenerCallTask;

    private WebViewDownloader webViewDownloader;

    @NonNull
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

            Executor serialExecutor = DependencyProvider.getInstance().provideSerialExecutor();
            criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(
                    criteoInterstitialAdListener);
            criteoInterstitialListenerCallTask.executeOnExecutor(serialExecutor, slot);

            if (slot != null) {
                //gets Webview data from Criteo before showing Interstitialview Activity
                getWebviewDataAsync(slot.getDisplayUrl());
            } else {
                webViewDownloader.downloadFailed();
            }
        }
    }

    private void getWebviewDataAsync(String displayUrl) {
        webViewDownloader.fillWebViewHtmlContent(
            displayUrl,
            deviceInfo,
            criteoInterstitialAdDisplayListener);
    }

    public String getWebViewDataContent() {
        return webViewDownloader.getWebViewData().getContent();
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL);

        criteoInterstitialListenerCallTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
        Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
        criteoInterstitialListenerCallTask.executeOnExecutor(threadPoolExecutor, tokenValue);

        if (tokenValue != null) {
            getWebviewDataAsync(tokenValue.getDisplayUrl());
        }
    }
}
