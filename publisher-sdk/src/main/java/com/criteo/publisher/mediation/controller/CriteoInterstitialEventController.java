package com.criteo.publisher.mediation.controller;

import android.content.Context;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.tasks.CriteoInterstitialFetchTask;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;


public class CriteoInterstitialEventController {

    private static final String DISPLAY_URL_MACRO = "%%displayUrl%%";

    private Context context;
    private boolean loaded;
    private CriteoInterstitialView criteoInterstitialview;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;
    private CriteoInterstitialFetchTask criteoInterstitialFetchTask;

    public CriteoInterstitialEventController(Context context, CriteoInterstitialView interstitialview,
            CriteoInterstitialAdListener listener) {
        this.criteoInterstitialview = interstitialview;
        this.criteoInterstitialAdListener = listener;
        this.context = context;
        this.loaded = false;
    }

    private boolean isAdLoaded() {
        return loaded;
    }

    void fetchAdAsync(AdUnit adUnit) {
        loaded = false;
        Slot slot = Criteo.getInstance().getBidForAdUnit(adUnit);

        criteoInterstitialFetchTask = new CriteoInterstitialFetchTask(criteoInterstitialAdListener);
        criteoInterstitialFetchTask.execute(slot);

        if (slot != null) {
            String displayUrlWithTag = Config.mediationAdTag;
            String displayUrl = displayUrlWithTag.replace(DISPLAY_URL_MACRO, slot.getDisplayUrl());

            //gets Webview data from Criteo before showing Interstitialview Activity
            getWebviewDataAsync(displayUrl);
        }
    }

    public void show() {

    }

    private void getWebviewDataAsync(String displayUrl) {

    }

}
