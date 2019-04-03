package com.criteo.publisher.mediation.controller;

import android.content.Context;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.tasks.CriteoInterstitialFetchTask;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;


public class CriteoInterstitialEventController {

    private Context context;
    private CriteoInterstitialView criteoInterstitialview;
    private CriteoInterstitialAdListener criteoInterstitialAdListener;
    private CriteoInterstitialFetchTask criteoInterstitialFetchTask;

    public CriteoInterstitialEventController(Context context, CriteoInterstitialView interstitialview,
            CriteoInterstitialAdListener listener) {
        this.criteoInterstitialview = interstitialview;
        this.criteoInterstitialAdListener = listener;
        this.context = context;
    }

    private boolean isAdLoaded() {
        return true;
    }

    void fetchAdAsync() {

    }

    public void show() {

    }
}
