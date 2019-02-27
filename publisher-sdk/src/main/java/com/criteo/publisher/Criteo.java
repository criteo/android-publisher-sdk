package com.criteo.publisher;

import android.app.Application;
import android.text.TextUtils;

import com.criteo.publisher.model.AdUnit;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.List;

public final class Criteo {
    private static final String EVENT_LAUNCH = "Launch";

    private static Criteo criteo;
    private BidManager bidManager;

    public static Criteo init(Application application, List<AdUnit> adUnits, int networkId) {
        synchronized (Criteo.class) {
            if (criteo == null) {
                criteo = new Criteo(application, adUnits, networkId);
            }
        }
        return criteo;
    }

    public static Criteo getInstance() {
        return criteo;
    }

    private Criteo(Application application, List<AdUnit> adUnits, int networkId) {
        if (application == null)
            throw new IllegalArgumentException("Application reference is required.");
        if (adUnits == null || adUnits.size() == 0)
            throw new IllegalArgumentException("AdUnits are required.");
        for (AdUnit adUnit : adUnits) {
            if (TextUtils.isEmpty(adUnit.getPlacementId()) || adUnit.getSize() == null
                    || adUnit.getSize().getWidth() <= 0 || adUnit.getSize().getHeight() <= 0) {
                throw new IllegalArgumentException("Found an invalid adUnit: " + adUnit);
            }
        }
        if (networkId == 0) throw new IllegalArgumentException("NetworkId is required.");
        this.bidManager = new BidManager(application.getApplicationContext(), networkId, adUnits);
        bidManager.prefetch();
        bidManager.postAppEvent(EVENT_LAUNCH);
    }

    public PublisherAdRequest.Builder setBidsForAdUnit(PublisherAdRequest.Builder request, AdUnit adUnit) {
        return bidManager.enrichBid(request, adUnit);
    }

}
