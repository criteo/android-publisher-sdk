package com.criteo.publisher;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.Util.ReflectionUtil;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.Util.UserAgentHandler;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitHelper;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.CdbDownloadTask;
import java.util.ArrayList;
import java.util.List;

public class BidManager implements NetworkResponseListener, ApplicationStoppedListener {

    private static String MOPUB_ADVIEW_CLASS = "com.mopub.mobileads.MoPubView";
    private static String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";
    private static String DFP_ADREQUEST_CLASS = "com.google.android.gms.ads.doubleclick.PublisherAdRequest$Builder";
    private static final String CRT_CPM = "crt_cpm";
    private static final String CRT_DISPLAY_URL = "crt_displayUrl";
    private static final int SECOND_TO_MILLI = 1000;
    private static final int PROFILE_ID = 235;
    private final List<CacheAdUnit> cacheAdUnits;
    private final Context mContext;
    private final SdkCache cache;
    private final TokenCache tokenCache;
    private final Publisher publisher;
    private final User user;
    private CdbDownloadTask cdbDownloadTask;
    private long cdbTimeToNextCall = 0;
    private Config config;
    private String userAgent;

    BidManager(Context context, String criteoPublisherId, List<CacheAdUnit> cacheAdUnits, TokenCache tokenCache) {
        this.mContext = context;
        this.cacheAdUnits = cacheAdUnits;
        this.cache = new SdkCache();
        this.tokenCache = tokenCache;
        publisher = new Publisher(mContext);
        publisher.setCriteoPublisherId(criteoPublisherId);
        user = new User();
        userAgent = "";
    }

    /**
     * load data for next time
     */
    private void prefetch(boolean callConfig, String userAgent, CacheAdUnit cacheAdUnit) {
        List<CacheAdUnit> prefetchCacheAdUnits = new ArrayList<CacheAdUnit>();
        prefetchCacheAdUnits.add(cacheAdUnit);
        if (cdbDownloadTask != null && cdbDownloadTask.getStatus() != AsyncTask.Status.RUNNING &&
                cdbTimeToNextCall < System.currentTimeMillis()) {
            startCdbDownloadTask(callConfig, prefetchCacheAdUnits);
        }
    }

    /**
     * Method to start new CdbDownload Asynctask
     */
    void startCdbDownloadTask(boolean callConfig, List<CacheAdUnit> prefetchCacheAdUnits) {
        cdbDownloadTask = new CdbDownloadTask(mContext, this, callConfig, userAgent);
        cdbDownloadTask.execute(PROFILE_ID, user, publisher, prefetchCacheAdUnits);
    }


    public void enrichBid(Object object, AdUnit adUnit) {
        if (config != null && config.isKillSwitch()) {
            return;
        }
        if (object != null) {
            if (object.getClass() == ReflectionUtil.getClassFromString(MOPUB_ADVIEW_CLASS)
                    || object.getClass() == ReflectionUtil.getClassFromString(MOPUB_INTERSTITIAL_CLASS)) {
                enrichMoPubBid(object, adUnit);
            } else if (object.getClass() == ReflectionUtil.getClassFromString(DFP_ADREQUEST_CLASS)) {
                enrichDfpBid(object, adUnit);
            }
        }
    }

    private void enrichMoPubBid(Object object, AdUnit adUnit) {
        Slot slot = getBidForAdUnitAndPrefetch(adUnit);
        if (slot != null && slot.isValid()) {
            StringBuilder keywords = new StringBuilder();
            Object existingKeywords = ReflectionUtil.callMethodOnObject(object, "getKeywords");
            if (existingKeywords != null) {
                keywords.append(existingKeywords);
                keywords.append(",");
            }
            keywords.append(CRT_CPM + ":" + slot.getCpm());
            keywords.append(",");
            keywords.append(CRT_DISPLAY_URL + ":" + slot.getDisplayUrl());
            ReflectionUtil.callMethodOnObject(object, "setKeywords", keywords.toString());
        }
    }


    private void enrichDfpBid(Object object, AdUnit adUnit) {
        Slot slot = getBidForAdUnitAndPrefetch(adUnit);
        if (slot != null && slot.isValid()) {
            ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_CPM, slot.getCpm());
            ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_DISPLAY_URL,
                    DeviceUtil.createDfpCompatibleDisplayUrl(slot.getDisplayUrl()));
        }
    }

    Slot getBidForAdUnitAndPrefetch(AdUnit adUnit) {
        if (adUnit == null) {
            Log.e(TAG, "AdUnit is required.");
            return null;
        }
        CacheAdUnit cacheAdUnit = AdUnitHelper
                .convertoCacheAdUnit(adUnit, mContext.getResources().getConfiguration().orientation);

        Slot peekSlot = cache.peekAdUnit(cacheAdUnit.getPlacementId(), cacheAdUnit.getSize().getFormattedSize());
        if (peekSlot == null) {
            prefetch(false, userAgent, cacheAdUnit);
            return null;
        }
        double cpm = (peekSlot.getCpmAsNumber() == null ? 0.0 : peekSlot.getCpmAsNumber());
        long ttl = peekSlot.getTtl();
        long expiryTimeMillis = ttl * SECOND_TO_MILLI + peekSlot.getTimeOfDownload();
        //If cpm and ttl in slot are 0:
        // Prefetch from CDB and do not update request;
        if (cpm == 0 && ttl == 0) {
            cache.remove(cacheAdUnit.getPlacementId(),
                    cacheAdUnit.getSize().getFormattedSize());
            prefetch(false, userAgent, cacheAdUnit);
            return null;
        }
        //If cpm is 0, ttl in slot > 0
        // we will stay silent until ttl expires;
        else if (cpm == 0 && ttl > 0
                && expiryTimeMillis > System.currentTimeMillis()) {
            return null;
        } else {
            //If cpm > 0, ttl > 0 but we are done staying silent
            Slot slot = cache.getAdUnit(cacheAdUnit.getPlacementId(),
                    cacheAdUnit.getSize().getFormattedSize());
            prefetch(false, userAgent, cacheAdUnit);
            return slot;
        }

    }


    @Override
    public void setCacheAdUnits(List<Slot> slots) {
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

    public BidResponse getBidForInhouseMediation(AdUnit adUnit) {
        BidResponse bidResponse = new BidResponse(0, null, false);
        Slot slot = this.getBidForAdUnitAndPrefetch(adUnit);
        if (slot != null && slot.isValid()) {

            TokenValue tokenValue = new TokenValue(slot.getTimeOfDownload(), slot.getTtl(), slot.getDisplayUrl(),
                    adUnit.getAdUnitType());

            double price = slot.getCpmAsNumber();

            bidResponse = new BidResponse(price, tokenCache.add(tokenValue), true);
        }

        return bidResponse;
    }


    /**
     * Method to post new Handler to the Main Thread When we get "useragent" from the Listener we start new CdbDownload
     * Asynctask to get Cdb and Config
     */
    protected void prefetch() {

        final Handler mainHandler = new UserAgentHandler(Looper.getMainLooper(), new UserAgentCallback() {
            @Override
            public void done(String useragent) {
                userAgent = useragent;
                startCdbDownloadTask(true, cacheAdUnits);

            }
        });

        final Runnable setUserAgentTask = new Runnable() {
            @Override
            public void run() {

                String taskUserAgent = DeviceUtil.getUserAgent(mContext);
                Message msg = mainHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("userAgent", taskUserAgent);
                msg.setData(bundle);
                mainHandler.sendMessage(msg);

            }

        };
        mainHandler.post(setUserAgentTask);

    }

    public TokenValue getTokenValue(BidToken bidToken, AdUnitType adUnitType) {
        if (bidToken != null) {
            return tokenCache.getTokenValue(bidToken, adUnitType);
        }
        return null;
    }
}
