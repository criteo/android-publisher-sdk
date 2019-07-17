package com.criteo.publisher;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.model.BannerAdUnit;

public class CriteoBannerView extends WebView {

    private static final String TAG = CriteoBannerView.class.getSimpleName();

    private BannerAdUnit bannerAdUnit;

    private CriteoBannerAdListener criteoBannerAdListener;

    private CriteoBannerEventController criteoBannerEventController;


    public CriteoBannerView(Context context, BannerAdUnit bannerAdUnit) {
        super(context);
        this.bannerAdUnit = bannerAdUnit;


    }

    public void setCriteoBannerAdListener(CriteoBannerAdListener criteoBannerAdListener) {
        this.criteoBannerAdListener = criteoBannerAdListener;

    }

    public void loadAd() {
        try {
            doLoadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while loading banner.", tr);
        }
    }

    private void doLoadAd() {
        if (criteoBannerEventController == null) {
            criteoBannerEventController =
                    new CriteoBannerEventController(this, criteoBannerAdListener);
        }
        criteoBannerEventController.fetchAdAsync(bannerAdUnit);
    }

    public void loadAd(BidToken bidToken) {
        try {
            doLoadAd(bidToken);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while loading banner from bid token.", tr);
        }
    }

    private void doLoadAd(BidToken bidToken) {
        if (criteoBannerEventController == null) {
            criteoBannerEventController =
                    new CriteoBannerEventController(this, criteoBannerAdListener);
        }
        criteoBannerEventController.fetchAdAsync(bidToken);
    }

    public void destroy() {
        super.destroy();
    }


}
