package com.criteo.pubsdk;

import android.content.Context;
import android.os.AsyncTask;
import com.criteo.pubsdk.cache.SdkCache;
import com.criteo.pubsdk.model.AdUnit;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;
import com.criteo.pubsdk.network.CdbDownloadTask;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.ArrayList;
import java.util.List;

class BidManager {
    private static final String CRT_CPM = "CRT_CPM";
    private static final String CRT_DISPLAY_URL = "CRT_displayUrl";
    private static final int PROFILE_ID = 235;
    private List<AdUnit> adUnits;
    private Context mContext;
    private CdbDownloadTask mTask;
    private SdkCache cache;
    private Publisher publisher;
    private User user;

    BidManager(Context context, int networkId, List<AdUnit> adUnits) {
        this.mContext = context;
        this.adUnits = adUnits;
        this.cache = new SdkCache();
        publisher = new Publisher(mContext);
        publisher.setNetworkId(networkId);
        mTask = new CdbDownloadTask(context, this.cache);
        user = new User(mContext);

    }

    void prefetch() {
        if (mTask.getStatus() != AsyncTask.Status.RUNNING) {
            mTask.execute(PROFILE_ID, user, publisher, this.adUnits);
        }

    }

    void cancelLoad() {
        if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }
    }

    PublisherAdRequest.Builder enrichBid(PublisherAdRequest.Builder request, AdUnit adUnit) {
        Slot slot = cache.getAdUnit(adUnit.getPlacementId(),
                adUnit.getSize().getWidth(), adUnit.getSize().getHeight());
        if (slot != null) {
            request.addCustomTargeting(CRT_CPM, String.valueOf(slot.getCpm()));
            request.addCustomTargeting(CRT_DISPLAY_URL, slot.getDisplayUrl());
        }
        if (mTask.getStatus() != AsyncTask.Status.RUNNING) {
            mTask = new CdbDownloadTask(mContext, this.cache);
            List<AdUnit> adUnits = new ArrayList<AdUnit>();
            adUnits.add(adUnit);
            mTask.execute(PROFILE_ID, user, publisher, adUnits);
        }
        return request;

    }
}
