package com.criteo.pubsdk;

import android.content.Context;
import android.text.TextUtils;

import com.criteo.pubsdk.model.AdUnit;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.List;

public final class Criteo {
    public static final String EVENT_LAUNCH = "Launch";

    private static Criteo criteo;
    private BidManager bidManager;

    public static Criteo init(Context context, List<AdUnit> adUnits, int networkId) {
        synchronized (Criteo.class) {
            if (criteo == null) {
                criteo = new Criteo(context, adUnits, networkId);
            }
        }
        return criteo;
    }

    private Criteo(Context context, List<AdUnit> adUnits, int networkId) {
        if (context == null) throw new IllegalArgumentException("Application context is required.");
        if (adUnits == null || adUnits.size() == 0)
            throw new IllegalArgumentException("AdUnits are required.");
        for (AdUnit adUnit : adUnits) {
            if (TextUtils.isEmpty(adUnit.getPlacementId()) || adUnit.getSize() == null
                    || adUnit.getSize().getWidth() <= 0 || adUnit.getSize().getHeight() <= 0) {
                throw new IllegalArgumentException("Found an invalid adUnit: " + adUnit);
            }
        }
        if (networkId == 0) throw new IllegalArgumentException("NetworkId is required.");
        this.bidManager = new BidManager(context, networkId, adUnits);
        bidManager.prefetch();
        bidManager.postAppEvent(EVENT_LAUNCH);
    }

    public PublisherAdRequest.Builder setBidsForAdUnit(PublisherAdRequest.Builder request, AdUnit adUnit) {
        return bidManager.enrichBid(request, adUnit);
    }

}
