package com.criteo.publisher;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.Util.UserAgentHandler;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.CdbDownloadTask;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.ArrayList;
import java.util.List;

public class BidManager implements NetworkResponseListener, ApplicationStoppedListener {
    private static final String CRT_CPM = "crt_cpm";
    private static final String CRT_DISPLAY_URL = "crt_displayUrl";
    private static final int PROFILE_ID = 235;
    private final List<AdUnit> adUnits;
    private final Context mContext;
    private final SdkCache cache;
    private final Publisher publisher;
    private final User user;
    private CdbDownloadTask cdbDownloadTask;
    private long cdbTimeToNextCall = 0;
    private Config config;
    private String userAgent;

    BidManager(Context context, int networkId, List<AdUnit> adUnits) {
        this.mContext = context;
        this.adUnits = adUnits;
        this.cache = new SdkCache();
        publisher = new Publisher(mContext);
        publisher.setNetworkId(networkId);
        user = new User();
        userAgent = "";
    }


    /**
     * Method to start new CdbDownload Asynctask
     *
     * @param callConfig
     * @param userAgent
     */
    private void startCdbDownloadTask(boolean callConfig, String userAgent, List<AdUnit> cdbAdUnits ) {
        cdbDownloadTask = new CdbDownloadTask(mContext, this, callConfig, userAgent);
        cdbDownloadTask.execute(PROFILE_ID, user, publisher, cdbAdUnits);
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
        if (cdbDownloadTask != null && cdbDownloadTask.getStatus() != AsyncTask.Status.RUNNING &&
                cdbTimeToNextCall < System.currentTimeMillis()) {
            List<AdUnit> enrichBidAdUnits = new ArrayList<AdUnit>();
            enrichBidAdUnits.add(adUnit);
            startCdbDownloadTask(false, userAgent, enrichBidAdUnits);
        }
        return request;
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

    @Override
    public void onApplicationStopped() {
        if (cdbDownloadTask != null && cdbDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
            cdbDownloadTask.cancel(true);
        }
    }


    /**
     * Method to post new Handler to the Main Thread
     *
     * When we get "useragent" from the Listener we start new CdbDownload Asynctask
     * to get Cdb and Config
     */
    protected void prefetch() {

        final Handler mainHandler = new UserAgentHandler(Looper.getMainLooper(), new UserAgentCallback() {
            @Override
            public void done(String useragent) {
                userAgent = useragent;
                startCdbDownloadTask(true, userAgent, adUnits);

            }
        });

        final Runnable setUserAgentTask = new Runnable() {
            @Override
            public void run() {

                String userAgent = DeviceUtil.getUserAgent(mContext);
                Message msg = mainHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("userAgent", userAgent);
                msg.setData(bundle);
                mainHandler.sendMessage(msg);

            }

        };
        mainHandler.post(setUserAgentTask);

    }


}
