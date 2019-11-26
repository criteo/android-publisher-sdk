package com.criteo.publisher;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.Util.ReflectionUtil;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitHelper;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.NativeAssets;
import com.criteo.publisher.model.NativeProduct;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.CdbDownloadTask;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Executor;
import org.json.JSONObject;

public class BidManager implements NetworkResponseListener, ApplicationStoppedListener {

    private static String MOPUB_ADVIEW_CLASS = "com.mopub.mobileads.MoPubView";
    private static String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";
    private static String DFP_ADREQUEST_CLASS = "com.google.android.gms.ads.doubleclick.PublisherAdRequest$Builder";

    private static final String CRT_CPM = "crt_cpm";
    private static final String CRT_DISPLAY_URL = "crt_displayUrl";
    private static final String CRT_NATIVE_TITLE = "crtn_title";
    private static final String CRT_NATIVE_DESC = "crtn_desc";
    private static final String CRT_NATIVE_PRICE = "crtn_price";
    private static final String CRT_NATIVE_CLICK_URL = "crtn_clickurl";
    private static final String CRT_NATIVE_CTA = "crtn_cta";
    private static final String CRT_NATIVE_IMAGE_URL = "crtn_imageurl";
    private static final String CRT_NATIVE_ADV_NAME = "crtn_advname";
    private static final String CRT_NATIVE_ADV_DOMAIN = "crtn_advdomain";
    private static final String CRT_NATIVE_ADV_LOGO_URL = "crtn_advlogourl";
    private static final String CRT_NATIVE_ADV_URL = "crtn_advurl";
    private static final String CRT_NATIVE_PR_URL = "crtn_prurl";
    private static final String CRT_NATIVE_PR_IMAGE_URL = "crtn_primageurl";
    private static final String CRT_NATIVE_PR_TEXT = "crtn_prtext";
    private static final String CRT_NATIVE_PIXEL_URL = "crtn_pixurl_";
    private static final String CRT_NATIVE_PIXEL_COUNT = "crtn_pixcount";


    private static final int SECOND_TO_MILLI = 1000;
    private static final int PROFILE_ID = 235;
    private final List<CacheAdUnit> cacheAdUnits;
    private final Context mContext;
    private final SdkCache cache;
    private final TokenCache tokenCache;
    private final Publisher publisher;
    private final User user;
    private long cdbTimeToNextCall = 0;
    private DeviceInfo deviceInfo;
    private Hashtable<CacheAdUnit, CdbDownloadTask> placementsWithCdbTasks;
    private final AndroidUtil androidUtil;
    private final Config config;
    private final AdvertisingInfo advertisingInfo;

    BidManager(
        @NonNull Context context,
        @NonNull String criteoPublisherId,
        @NonNull List<CacheAdUnit> cacheAdUnits,
        @NonNull DeviceInfo deviceInfo,
        @NonNull Config config,
        @NonNull AndroidUtil androidUtil,
        @NonNull AdvertisingInfo advertisingInfo
    ) {
        this(
            context,
            new Publisher(context, criteoPublisherId),
            cacheAdUnits,
            new TokenCache(),
            deviceInfo,
            new User(),
            new SdkCache(),
            new Hashtable<>(),
            config,
            androidUtil,
            advertisingInfo
        );
    }

    @VisibleForTesting
    BidManager(
        @NonNull Context context,
        @NonNull Publisher publisher,
        @NonNull List<CacheAdUnit> cacheAdUnits,
        @NonNull TokenCache tokenCache,
        @NonNull DeviceInfo deviceInfo,
        @NonNull User user,
        @NonNull SdkCache sdkCache,
        @NonNull Hashtable<CacheAdUnit, CdbDownloadTask> placementsWithCdbTasks,
        @NonNull Config config,
        @NonNull AndroidUtil androidUtil,
        @NonNull AdvertisingInfo advertisingInfo
    ) {
        this.mContext = context;
        this.publisher = publisher;
        this.cacheAdUnits = cacheAdUnits;
        this.tokenCache = tokenCache;
        this.deviceInfo = deviceInfo;
        this.user = user;
        this.cache = sdkCache;
        this.placementsWithCdbTasks = placementsWithCdbTasks;
        this.androidUtil = androidUtil;
        this.config = config;
        this.advertisingInfo = advertisingInfo;
    }

    /**
     * load data for next time
     */
    private void fetch(CacheAdUnit cacheAdUnit) {
        if (placementsWithCdbTasks.containsKey(cacheAdUnit)) {
            return;
        }

        if (cdbTimeToNextCall < System.currentTimeMillis()) {
            ArrayList<CacheAdUnit> cacheAdUnitsForPrefetch = new ArrayList<>();
            cacheAdUnitsForPrefetch.add(cacheAdUnit);
            startCdbDownloadTask(false, cacheAdUnitsForPrefetch);
        }
    }

    /**
     * Method to start new CdbDownload Asynctask
     */
    private void startCdbDownloadTask(boolean callConfig, List<CacheAdUnit> prefetchCacheAdUnits) {
        CdbDownloadTask cdbDownloadTask = new CdbDownloadTask(
            mContext,
            this,
            callConfig,
            deviceInfo.getUserAgent(),
            prefetchCacheAdUnits,
            placementsWithCdbTasks,
            advertisingInfo
        );

        for (CacheAdUnit cacheAdUnit : prefetchCacheAdUnits) {
            placementsWithCdbTasks.put(cacheAdUnit, cdbDownloadTask);
        }

        Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
        cdbDownloadTask.executeOnExecutor(threadPoolExecutor, PROFILE_ID, user, publisher);
    }


    public void enrichBid(Object object, AdUnit adUnit) {
        if (killSwitchEngaged()) {
            return;
        }
        if (object != null) {
            if (object.getClass() == ReflectionUtil.getClassFromString(MOPUB_ADVIEW_CLASS)
                    || object.getClass() == ReflectionUtil.getClassFromString(MOPUB_INTERSTITIAL_CLASS)) {
                enrichMoPubBid(object, adUnit);
            } else if (object.getClass() == ReflectionUtil.getClassFromString(DFP_ADREQUEST_CLASS)) {
                enrichDfpBid(object, adUnit);
            } else if (object instanceof Map) {
                enrichMapBid((Map)object, adUnit);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void enrichMapBid(Map map, AdUnit adUnit)
    {
        Slot slot = getBidForAdUnitAndPrefetch(adUnit);
        if (slot != null && slot.isValid()) {
            map.put(CRT_DISPLAY_URL, slot.getDisplayUrl());
            map.put(CRT_CPM, slot.getCpm());
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
            keywords.append(CRT_CPM);
            keywords.append(":");
            keywords.append(slot.getCpm());
            keywords.append(",");
            keywords.append(CRT_DISPLAY_URL);
            keywords.append(":");
            keywords.append(slot.getDisplayUrl());
            ReflectionUtil.callMethodOnObject(object, "setKeywords", keywords.toString());
        }
    }

    private void enrichDfpBid(Object object, AdUnit adUnit) {
        Slot slot = getBidForAdUnitAndPrefetch(adUnit);
        if (slot != null) {
            if (!slot.isValid()) {
                return;
            }

            ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_CPM, slot.getCpm());

            if (slot.isNative()) {
                enrichNativeRequest(slot, object);

            } else {
                enrichRequest(slot, object);
            }

        }
    }

    //Banner and Interstitial slot
    private void enrichRequest(Slot slot, Object object) {
        ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_DISPLAY_URL,
                DeviceUtil.createDfpCompatibleString(slot.getDisplayUrl()));
    }

    //Native slot
    private void enrichNativeRequest(Slot slot, Object object) {
        NativeAssets nativeAssets = slot.getNativeAssets();
        if (nativeAssets == null) {
            return;
        }

        //reflect first product fields
        if (nativeAssets.nativeProducts != null && nativeAssets.nativeProducts.size() > 0) {
            NativeProduct product = nativeAssets.nativeProducts.get(0);

            checkAndReflect(object, product.title, CRT_NATIVE_TITLE);
            checkAndReflect(object, product.description, CRT_NATIVE_DESC);
            checkAndReflect(object, product.price, CRT_NATIVE_PRICE);
            checkAndReflect(object, product.clickUrl, CRT_NATIVE_CLICK_URL);
            checkAndReflect(object, product.callToAction, CRT_NATIVE_CTA);
            checkAndReflect(object, product.imageUrl, CRT_NATIVE_IMAGE_URL);
        }

        //reflect advertiser fields
        checkAndReflect(object, nativeAssets.advertiserDescription, CRT_NATIVE_ADV_NAME);
        checkAndReflect(object, nativeAssets.advertiserDomain, CRT_NATIVE_ADV_DOMAIN);
        checkAndReflect(object, nativeAssets.advertiserLogoUrl, CRT_NATIVE_ADV_LOGO_URL);
        checkAndReflect(object, nativeAssets.advertiserLogoClickUrl, CRT_NATIVE_ADV_URL);

        //reflect privacy fields
        checkAndReflect(object, nativeAssets.privacyOptOutClickUrl, CRT_NATIVE_PR_URL);
        checkAndReflect(object, nativeAssets.privacyOptOutImageUrl, CRT_NATIVE_PR_IMAGE_URL);
        checkAndReflect(object, nativeAssets.privacyLongLegalText, CRT_NATIVE_PR_TEXT);

        //reflect impression pixels
        if (nativeAssets.impressionPixels != null && nativeAssets.impressionPixels.size() > 0) {
            for (int i = 0; i < nativeAssets.impressionPixels.size(); i++) {
                checkAndReflect(object, nativeAssets.impressionPixels.get(i), CRT_NATIVE_PIXEL_URL + i);
            }
        }

        ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_NATIVE_PIXEL_COUNT,
                nativeAssets.impressionPixels.size() + "");

    }

    Slot getBidForAdUnitAndPrefetch(AdUnit adUnit) {
        if (adUnit == null) {
            Log.e(TAG, "AdUnit is required.");
            return null;
        }
        if (killSwitchEngaged()) {
            return null;
        }
        CacheAdUnit cacheAdUnit = AdUnitHelper
                .convertoCacheAdUnit(adUnit, androidUtil.getOrientation());

        Slot peekSlot = cache.peekAdUnit(cacheAdUnit);
        if (peekSlot == null) {
            fetch(cacheAdUnit);
            return null;
        }
        double cpm = (peekSlot.getCpmAsNumber() == null ? 0.0 : peekSlot.getCpmAsNumber());
        long ttl = peekSlot.getTtl();
        long expiryTimeMillis = ttl * SECOND_TO_MILLI + peekSlot.getTimeOfDownload();
        //If cpm and ttl in slot are 0:
        // Prefetch from CDB and do not update request;
        if (cpm == 0 && ttl == 0) {
            cache.remove(cacheAdUnit);
            fetch(cacheAdUnit);
            return null;
        }
        //If cpm is 0, ttl in slot > 0
        // we will stay silent until ttl expires;
        else if (cpm == 0 && ttl > 0
                && expiryTimeMillis > System.currentTimeMillis()) {
            return null;
        } else {
            //If cpm > 0, ttl > 0 but we are done staying silent
            Slot slot = cache.getAdUnit(cacheAdUnit);
            fetch(cacheAdUnit);
            return slot;
        }

    }


    private void checkAndReflect(Object object, String fieldName, String enrichmentKey) {
        if (!TextUtils.isEmpty(fieldName)) {
            ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", enrichmentKey,
                    DeviceUtil.createDfpCompatibleString(fieldName));
        }
    }

    @Override
    public void setCacheAdUnits(List<Slot> slots) {
        cache.addAll(slots);
    }

    @Override
    public void refreshConfig(JSONObject configJSONObject) {
        config.refreshConfig(configJSONObject);
    }

    @Override
    public void setTimeToNextCall(int seconds) {
        this.cdbTimeToNextCall = System.currentTimeMillis() + seconds * 1000;
    }

    @Override
    public void onApplicationStopped() {
        for (CacheAdUnit key : placementsWithCdbTasks.keySet()) {
            if (placementsWithCdbTasks.get(key) != null
                    && placementsWithCdbTasks.get(key).getStatus() == AsyncTask.Status.RUNNING) {
                (placementsWithCdbTasks.get(key)).cancel(true);
            }
        }
    }

    public BidResponse getBidForInhouseMediation(AdUnit adUnit) {
        BidResponse bidResponse = new BidResponse();
        Slot slot = this.getBidForAdUnitAndPrefetch(adUnit);
        if (slot != null && slot.isValid()) {

            TokenValue tokenValue = new TokenValue(slot.getTimeOfDownload(), slot.getTtl(), slot.getDisplayUrl(),
                    adUnit.getAdUnitType());

            double price = slot.getCpmAsNumber();

            bidResponse = new BidResponse(price, tokenCache.add(tokenValue, adUnit), true);
        }

        return bidResponse;
    }


    /**
     * This method is called back after the "useragent" is fetched
     */
    protected void prefetch() {
        startCdbDownloadTask(true, cacheAdUnits);
    }

    public TokenValue getTokenValue(BidToken bidToken, AdUnitType adUnitType) {
        if (bidToken != null) {
            return tokenCache.getTokenValue(bidToken, adUnitType);
        }
        return null;
    }

    private boolean killSwitchEngaged() {
        return config.isKillSwitchEnabled();
    }
}
