package com.criteo.publisher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.webkit.WebView;
import com.criteo.publisher.Util.ObjectsUtil;
import com.criteo.publisher.model.BannerAdUnit;

public class CriteoBannerView extends WebView {

    private static final String TAG = CriteoBannerView.class.getSimpleName();

    @Nullable
    private final BannerAdUnit bannerAdUnit;

    /**
     * Null means that the singleton Criteo should be used.
     *
     * {@link Criteo#getInstance()} is fetched lazily so publishers may call the constructor without
     * having to init the SDK before.
     */
    @Nullable
    private final Criteo criteo;

    @Nullable
    private CriteoBannerAdListener criteoBannerAdListener;

    @Nullable
    private CriteoBannerEventController criteoBannerEventController;

    public CriteoBannerView(@NonNull Context context, @Nullable BannerAdUnit bannerAdUnit) {
        this(context, bannerAdUnit, null);
    }

    @VisibleForTesting
    CriteoBannerView(@NonNull Context context, @Nullable BannerAdUnit bannerAdUnit, @Nullable Criteo criteo) {
        super(context);
        this.bannerAdUnit = bannerAdUnit;
        this.criteo = criteo;
    }

    public void setCriteoBannerAdListener(@Nullable CriteoBannerAdListener criteoBannerAdListener) {
        this.criteoBannerAdListener = criteoBannerAdListener;
    }

    @Nullable
    CriteoBannerAdListener getCriteoBannerAdListener() {
        return criteoBannerAdListener;
    }

    public void loadAd() {
        try {
            doLoadAd();
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while loading banner.", tr);
        }
    }

    private void doLoadAd() {
        getOrCreateController().fetchAdAsync(bannerAdUnit);
    }

    public void loadAd(@Nullable BidToken bidToken) {
        try {
            doLoadAd(bidToken);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal error while loading banner from bid token.", tr);
        }
    }

    private void doLoadAd(@Nullable BidToken bidToken) {
        if (bidToken != null && !ObjectsUtil.equals(bannerAdUnit, bidToken.getAdUnit())) {
            return;
        }

        getOrCreateController().fetchAdAsync(bidToken);
    }

    @NonNull
    @VisibleForTesting
    CriteoBannerEventController getOrCreateController() {
        if (criteoBannerEventController == null) {
            criteoBannerEventController = new CriteoBannerEventController(this, getCriteo());
        }
        return criteoBannerEventController;
    }

    @NonNull
    private Criteo getCriteo() {
        return criteo == null ? Criteo.getInstance() : criteo;
    }

}
