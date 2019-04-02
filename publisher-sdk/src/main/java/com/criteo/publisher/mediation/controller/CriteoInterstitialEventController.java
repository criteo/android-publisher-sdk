package com.criteo.publisher.mediation.controller;

import android.content.Context;

import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.tasks.CriteoBannerFetchTask;
import com.criteo.publisher.mediation.view.CriteoBannerView;


public class CriteoInterstitialEventController {

    private CriteoBannerView criteoBannerView;

    private CriteoBannerFetchTask criteoBannerFetchTask;

    public CriteoInterstitialEventController(Context context, CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        bannerView.setCriteoBannerAdListener(listener);
    }

    private boolean isAdLoaded() {
        return true;
    }

    void fetchAdAsync() {

    }

    public void show() {

    }
}
