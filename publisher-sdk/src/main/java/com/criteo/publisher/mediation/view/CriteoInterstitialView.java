package com.criteo.publisher.mediation.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.criteo.publisher.mediation.controller.CriteoInterstitialEventController;
import com.criteo.publisher.mediation.controller.WebViewDownloader;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.WebviewData;

public class CriteoInterstitialView {

    private AdUnit adUnit;

    private Context context;

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    private CriteoInterstitialEventController criteoInterstitialEventController;


    public CriteoInterstitialView(Context context, AdUnit adUnit) {
        this.context = context;
        this.adUnit = adUnit;
    }

    public void setCriteoInterstitialAdListener(CriteoInterstitialAdListener criteoInterstitialAdListener) {
        this.criteoInterstitialAdListener = criteoInterstitialAdListener;

    }

    public void loadAd() {
        if (criteoInterstitialEventController == null) {
            criteoInterstitialEventController = new CriteoInterstitialEventController(context, this,
                    criteoInterstitialAdListener, new WebViewDownloader(new WebviewData()));
        }
        criteoInterstitialEventController.fetchAdAsync(adUnit);
    }

    public boolean isAdLoaded() {
        return criteoInterstitialEventController.isAdLoaded();
    }

    public void show() {
        if (isAdLoaded()) {
            Intent intent = new Intent(context, CriteoInterstitialActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("webviewdata", criteoInterstitialEventController.getWebViewDataContent());
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

}
