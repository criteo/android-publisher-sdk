package com.criteo.publisher.mediation.view;

import android.content.Context;
import android.webkit.WebView;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.mediation.controller.CriteoBannerEventController;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.model.BidToken;
import java.lang.ref.WeakReference;

public class CriteoBannerView extends WebView {

    private BannerAdUnit bannerAdUnit;

    private CriteoBannerAdListener criteoBannerAdListener;

    private WeakReference<CriteoBannerEventController> bannerController;

    private CriteoBannerEventController criteoBannerEventController;


    public CriteoBannerView(Context context, BannerAdUnit bannerAdUnit) {
        super(context);
        this.bannerAdUnit = bannerAdUnit;


    }

    public void setCriteoBannerAdListener(CriteoBannerAdListener criteoBannerAdListener) {
        this.criteoBannerAdListener = criteoBannerAdListener;

    }

    public void loadAd() {
        if (criteoBannerEventController == null) {
            criteoBannerEventController = new CriteoBannerEventController(this, criteoBannerAdListener);
        }
        criteoBannerEventController.fetchAdAsync(bannerAdUnit);
    }

    public void loadAd(BidToken bidToken) {
        if (criteoBannerEventController == null) {
            criteoBannerEventController = new CriteoBannerEventController(this, criteoBannerAdListener);
        }
        criteoBannerEventController.fetchAdAsync(bidToken);
    }


}
