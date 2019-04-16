package com.criteo.publisher.mediation.controller;

import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.tasks.CriteoBannerFetchTask;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Slot;


public class CriteoBannerEventController {

    private CriteoBannerView bannerView;

    private CriteoBannerAdListener listener;

    public CriteoBannerEventController(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.bannerView = bannerView;
        this.listener = listener;
    }

    public void notifyListenerAsync() {
        Slot slot = null;
        //get slot using validateAndPrefetchSlotInCache

        //load webview

        //calling fetchtask to notify listeners
        CriteoBannerFetchTask criteoBannerFetchTask = new CriteoBannerFetchTask(bannerView, listener);
        criteoBannerFetchTask.execute(slot);
    }

}
