package com.criteo.publisher.mediation.view;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import com.criteo.publisher.BidToken;
import com.criteo.publisher.CriteoBannerEventController;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.model.BannerAdUnit;
import java.lang.ref.WeakReference;

public class CriteoBannerView extends WebView {
    private static final String TAG = CriteoBannerView.class.getSimpleName();

    private BannerAdUnit bannerAdUnit;

    private CriteoBannerAdListener criteoBannerAdListener;

    private WeakReference<CriteoBannerEventController> criteoBannerEventController;


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
            criteoBannerEventController = new WeakReference<>(
                    new CriteoBannerEventController(this, criteoBannerAdListener));
        }
        criteoBannerEventController.get().fetchAdAsync(bannerAdUnit);
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
            criteoBannerEventController = new WeakReference<>(
                    new CriteoBannerEventController(this, criteoBannerAdListener));
        }
        criteoBannerEventController.get().fetchAdAsync(bidToken);
    }

    public void destroy() {
        super.destroy();
    }


}
