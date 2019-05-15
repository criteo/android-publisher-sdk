package com.criteo.publisher;

import android.app.Application;
import android.text.TextUtils;

import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.Util.AppLifecycleUtil;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;

import java.util.List;

public final class Criteo {

    private static Criteo criteo;
    private BidManager bidManager;
    private AppEvents appEvents;
    private AppLifecycleUtil appLifecycleUtil;

    public static Criteo init(Application application, List<AdUnit> adUnits, int criteoPublisherId) {
        synchronized (Criteo.class) {
            if (criteo == null) {
                criteo = new Criteo(application, adUnits, criteoPublisherId);
            }
        }
        return criteo;
    }

    public static Criteo getInstance() {
        return criteo;
    }

    private Criteo(Application application, List<AdUnit> adUnits, int criteoPublisherId) {
        if (application == null) {
            throw new IllegalArgumentException("Application reference is required.");
        }
        if (adUnits == null || adUnits.size() == 0) {
            throw new IllegalArgumentException("AdUnits are required.");
        }
        for (AdUnit adUnit : adUnits) {
            if (TextUtils.isEmpty(adUnit.getPlacementId()) || adUnit.getSize() == null
                    || adUnit.getSize().getWidth() <= 0 || adUnit.getSize().getHeight() <= 0) {
                throw new IllegalArgumentException("Found an invalid adUnit: " + adUnit);
            }
        }
        if (criteoPublisherId == 0) {
            throw new IllegalArgumentException("Criteo Publisher Id is required.");
        }
        this.bidManager = new BidManager(application.getApplicationContext(), criteoPublisherId, adUnits);
        this.appEvents = new AppEvents(application.getApplicationContext());
        this.appLifecycleUtil = new AppLifecycleUtil(application, appEvents, bidManager);
        bidManager.prefetch();
    }

    public void setBidsForAdUnit(Object object, AdUnit adUnit) {
        bidManager.enrichBid(object, adUnit);
    }

    /**
     * Method to start new CdbDownload Asynctask
     */
    public Slot getBidForAdUnit(AdUnit adUnit) {
        return bidManager.getBidForAdUnitAndPrefetch(adUnit);
    }

}
