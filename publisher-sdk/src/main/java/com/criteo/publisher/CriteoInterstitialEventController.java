package com.criteo.publisher;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.tasks.CriteoInterstitialListenerCallTask;
import java.util.concurrent.Executor;


public class CriteoInterstitialEventController {

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

    private CriteoInterstitialListenerCallTask criteoInterstitialListenerCallTask;

    private WebViewData webViewData;

    @NonNull
    private DeviceInfo deviceInfo;

    private final Criteo criteo;

    public CriteoInterstitialEventController(
            CriteoInterstitialAdListener listener, CriteoInterstitialAdDisplayListener adDisplayListener,
            WebViewData webViewData,
            Criteo criteo) {
        this.criteoInterstitialAdListener = listener;
        this.criteoInterstitialAdDisplayListener = adDisplayListener;
        this.webViewData = webViewData;
        this.deviceInfo = criteo.getDeviceInfo();
        this.criteo = criteo;
    }

    public boolean isAdLoaded() {
        return webViewData.isLoaded();
    }

    public void refresh() {
        webViewData.refresh();
    }

    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = null;
        if (!webViewData.isLoading()) {
            webViewData.downloadLoading();

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
                webViewData.downloadFailed();
            }
        }
    }

    private void getWebviewDataAsync(String displayUrl) {
        webViewData.fillWebViewHtmlContent(
            displayUrl,
            deviceInfo,
            criteoInterstitialAdDisplayListener);
    }

    public String getWebViewDataContent() {
        return webViewData.getContent();
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
