package com.criteo.publisher.mediation.view;

import android.content.Context;
import android.webkit.WebView;
import com.criteo.publisher.mediation.controller.CriteoBannerEventController;
import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.model.AdUnit;
import java.lang.ref.WeakReference;

public class CriteoBannerView extends WebView {

    private AdUnit adUnit;

    private CriteoBannerAdListener criteoBannerAdListener;

    private WeakReference<CriteoBannerEventController> bannerController;

    private CriteoBannerEventController criteoBannerEventController;


    public CriteoBannerView(Context context, AdUnit adUnit) {
        super(context);
        this.adUnit = adUnit;


    }

    public void setCriteoBannerAdListener(CriteoBannerAdListener criteoBannerAdListener) {
        this.criteoBannerAdListener = criteoBannerAdListener;

    }

    public void loadAd() {
        if (criteoBannerEventController == null) {
            criteoBannerEventController = new CriteoBannerEventController(this, criteoBannerAdListener);
        }
        criteoBannerEventController.fetchAdAsync(adUnit);
    }

}
