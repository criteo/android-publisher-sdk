package com.criteo.publisher.mediation.view;

import android.content.Context;
import android.webkit.WebView;

import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.controller.CriteoBannerEventController;
import com.google.android.gms.ads.AdSize;

import java.lang.ref.WeakReference;

public class CriteoBannerView extends WebView {

    private AdSize adSize;

    private String adUnit;

    private CriteoBannerAdListener criteoBannerAdListener;

    private WeakReference<CriteoBannerEventController> bannerController;


    public CriteoBannerView(Context context) {
        super(context);
    }

    public void setCriteoBannerAdListener(CriteoBannerAdListener criteoBannerAdListener) {
        this.criteoBannerAdListener = criteoBannerAdListener;
    }


    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setAdUnit(String adUnit) {
        this.adUnit = adUnit;
    }

    public AdSize getAdSize() {
        return adSize;
    }

    public String getAdUnit() {
        return adUnit;
    }

    void loadAd() {

    }
}
