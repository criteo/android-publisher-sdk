package com.criteo.publisher.mediation.controller;

import android.content.Context;

import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.tasks.CriteoBannerFetchTask;
import com.criteo.publisher.mediation.view.CriteoBannerView;


public class CriteoBannerEventController {

    public CriteoBannerView criteoBannerView;

    private CriteoBannerFetchTask criteoBannerFetchTask;

    public CriteoBannerEventController(Context context, CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        bannerView.setCriteoBannerAdListener(listener);
    }


    void fetchAdAsync() {

    }
}
