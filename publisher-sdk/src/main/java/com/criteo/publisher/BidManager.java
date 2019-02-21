package com.criteo.publisher;

import android.content.Context;
import android.os.AsyncTask;

import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.AppEventTask;
import com.criteo.publisher.network.CdbDownloadTask;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.ArrayList;
import java.util.List;

class BidManager implements NetworkResponseListener {
    private static final String CRT_CPM = "crt_cpm";
    private static final String CRT_DISPLAY_URL = "crt_displayUrl";
    private static final int PROFILE_ID = 235;
    private final List<AdUnit> adUnits;
    private final Context mContext;
    private final SdkCache cache;
    private final Publisher publisher;
    private final User user;
    private CdbDownloadTask cdbDownloadTask;
    private AppEventTask eventTask;
    private int appEventThrottle = -1;
    private long throttleSetTime = 0;
    private long cdbTimeToNextCall = 0;
    private Config config;

    BidManager(Context context, int networkId, List<AdUnit> adUnits) {
        this.mContext = context;
        this.adUnits = adUnits;
        this.cache = new SdkCache();
        publisher = new Publisher(mContext);
        publisher.setNetworkId(networkId);
        cdbDownloadTask = new CdbDownloadTask(context, this, true, DeviceUtil.getUserAgent(mContext));
        user = new User();
        eventTask = new AppEventTask(context, this);
    }

    void prefetch() {
        if (cdbDownloadTask.getStatus() != AsyncTask.Status.RUNNING
                && cdbDownloadTask.getStatus() != AsyncTask.Status.FINISHED) {
            cdbDownloadTask.execute(PROFILE_ID, user, publisher, adUnits);
        }
    }

    void postAppEvent(String eventType) {
        if (appEventThrottle > 0 &&
                System.currentTimeMillis() - throttleSetTime < appEventThrottle * 1000) {
            return;
        }
        if (eventTask.getStatus() == AsyncTask.Status.FINISHED) {
            eventTask = new AppEventTask(mContext, this);
        }
        if (eventTask.getStatus() != AsyncTask.Status.RUNNING) {
            eventTask.execute(eventType);
        }

    }

    PublisherAdRequest.Builder enrichBid(PublisherAdRequest.Builder request, AdUnit adUnit) {
        if (config != null && config.isKillSwitch()) {
            return request;
        }
        Slot slot = cache.getAdUnit(adUnit.getPlacementId(),
                adUnit.getSize().getFormattedSize());

        if (slot != null) {
            request.addCustomTargeting(CRT_CPM, slot.getCpm());
            request.addCustomTargeting(CRT_DISPLAY_URL, DeviceUtil.createDfpCompatibleDisplayUrl(slot.getDisplayUrl()));
        }
        if (cdbDownloadTask.getStatus() != AsyncTask.Status.RUNNING &&
                cdbTimeToNextCall < System.currentTimeMillis()) {
            cdbDownloadTask = new CdbDownloadTask(mContext, this, false, DeviceUtil.getUserAgent(mContext));
            List<AdUnit> adUnits = new ArrayList<AdUnit>();
            adUnits.add(adUnit);
            cdbDownloadTask.execute(PROFILE_ID, user, publisher, adUnits);
        }
        return request;
    }

    @Override
    public void setThrottle(int throttle) {
        this.appEventThrottle = throttle;
        this.throttleSetTime = System.currentTimeMillis();
    }

    @Override
    public void setAdUnits(List<Slot> slots) {
        cache.addAll(slots);
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void setTimeToNextCall(int seconds) {
        this.cdbTimeToNextCall = System.currentTimeMillis() + seconds * 1000;
    }
}
