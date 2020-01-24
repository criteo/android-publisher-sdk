package com.criteo.publisher;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.ApplicationStoppedListener;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.Util.ReflectionUtil;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitMapper;
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
import com.criteo.publisher.network.PubSdkApi;
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
    private static final String DFP_CRT_DISPLAY_URL = "crt_displayurl";
    private static final String MOPUB_CRT_DISPLAY_URL = "crt_displayUrl";
    private static final String MAP_CRT_DISPLAY_URL = "crt_displayUrl";

    /**
     * Default TTL (15 minutes in seconds) overridden on immediate bids (CPM > 0, TTL = 0).
     */
    private static final int DEFAULT_TTL_IN_SECONDS = 15 * 60;

    private static final int SECOND_TO_MILLI = 1000;
    private static final int PROFILE_ID = 235;
    private final SdkCache cache;
    private final TokenCache tokenCache;
    private final Publisher publisher;
    private final User user;
    private long cdbTimeToNextCall = 0;
    private DeviceInfo deviceInfo;
    private Hashtable<CacheAdUnit, CdbDownloadTask> placementsWithCdbTasks;
    private final DeviceUtil deviceUtil;
    private final LoggingUtil loggingUtil;
    private final Config config;

    @NonNull
    private final Clock clock;
    private final UserPrivacyUtil userPrivacyUtil;
    private final AdUnitMapper adUnitMapper;
    private final PubSdkApi api;

    BidManager(
        @NonNull Publisher publisher,
        @NonNull TokenCache tokenCache,
        @NonNull DeviceInfo deviceInfo,
        @NonNull User user,
        @NonNull SdkCache sdkCache,
        @NonNull Hashtable<CacheAdUnit, CdbDownloadTask> placementsWithCdbTasks,
        @NonNull Config config,
        @NonNull DeviceUtil deviceUtil,
        @NonNull LoggingUtil loggingUtil,
        @NonNull Clock clock,
        @NonNull UserPrivacyUtil userPrivacyUtil,
        @NonNull AdUnitMapper adUnitMapper,
        @NonNull PubSdkApi api
    ) {
        this.publisher = publisher;
        this.tokenCache = tokenCache;
        this.deviceInfo = deviceInfo;
        this.user = user;
        this.cache = sdkCache;
        this.placementsWithCdbTasks = placementsWithCdbTasks;
        this.config = config;
        this.deviceUtil = deviceUtil;
        this.loggingUtil = loggingUtil;
        this.clock = clock;
        this.userPrivacyUtil = userPrivacyUtil;
        this.adUnitMapper = adUnitMapper;
        this.api = api;
    }

    /**
     * load data for next time
     */
    private void fetch(CacheAdUnit cacheAdUnit) {
        if (placementsWithCdbTasks.containsKey(cacheAdUnit)) {
            return;
        }

        if (cdbTimeToNextCall < clock.getCurrentTimeInMillis()) {
            ArrayList<CacheAdUnit> cacheAdUnitsForPrefetch = new ArrayList<>();
            cacheAdUnitsForPrefetch.add(cacheAdUnit);
            startCdbDownloadTask(false, cacheAdUnitsForPrefetch);
        }
    }

    /**
     * Method to start new CdbDownload Asynctask
     */
    private void startCdbDownloadTask(boolean isConfigRequested, List<CacheAdUnit> prefetchCacheAdUnits) {
        boolean isCdbRequested = !killSwitchEngaged() && !prefetchCacheAdUnits.isEmpty();

        CdbDownloadTask cdbDownloadTask = new CdbDownloadTask(
            this,
            isConfigRequested,
            isCdbRequested,
            deviceInfo,
            prefetchCacheAdUnits,
            placementsWithCdbTasks,
            deviceUtil,
            loggingUtil,
            userPrivacyUtil,
            api
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
        if (slot == null) {
            return;
        }

        map.put(MAP_CRT_DISPLAY_URL, slot.getDisplayUrl());
        map.put(CRT_CPM, slot.getCpm());
    }

    private void enrichMoPubBid(Object object, AdUnit adUnit) {
        Slot slot = getBidForAdUnitAndPrefetch(adUnit);
        if (slot == null) {
            return;
        }

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
        keywords.append(MOPUB_CRT_DISPLAY_URL);
        keywords.append(":");
        keywords.append(slot.getDisplayUrl());
        ReflectionUtil.callMethodOnObject(object, "setKeywords", keywords.toString());
    }

    private void enrichDfpBid(Object object, AdUnit adUnit) {
        Slot slot = getBidForAdUnitAndPrefetch(adUnit);
        if (slot == null) {
            return;
        }

        ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", CRT_CPM, slot.getCpm());

        if (slot.isNative()) {
            enrichNativeRequest(slot, object);

        } else {
            enrichDfpRequest(slot, object);
        }
    }

    //Banner and Interstitial slot
    private void enrichDfpRequest(Slot slot, Object object) {
        ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", DFP_CRT_DISPLAY_URL,
                deviceUtil.createDfpCompatibleString(slot.getDisplayUrl()));
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

    /**
     * Returns the last fetched bid a fetch a new one for the next invocation.
     * <p>
     * A <code>null</code> value could be returned. This means that there is no valid bid for the
     * given {@link AdUnit}. And caller should not try to display anything.
     * <code>null</code> may be returned in case of
     * <ul>
     *   <li>The kill switch is engaged. See {@link Config#isKillSwitchEnabled()}</li>
     *   <li>The given {@link AdUnit} is not valid. See {@link AdUnitMapper} for validity definition</li>
     *   <li>There is no last fetch bid or last is consumed</li>
     *   <li>Last fetch bid correspond to a no-bid (CPM = 0 and TTL = 0)</li>
     *   <li>Last fetch bid is a not-expired silence (CPM = 0 and TTL > 0)</li>
     *   <li>Last fetch bid is expired</li>
     * </ul>
     * <p>
     * Asynchronously, a new bid is fetch to CDB to get a new proposition. Hence if this method
     * returns a bid, it is consumed, and you have to wait for the new proposition to get a result
     * again. Meanwhile, you'll only get a <code>null</code> value.
     * There may be some case when a new bid is not fetch:
     * <ul>
     *   <li>The kill switch is engaged</li>
     *   <li>The given {@link AdUnit} is not valid</li>
     *   <li>Last fetch bid is a not-expired silence</li>
     *   <li>There is already an async call to CDB for the given {@link AdUnit}</li>
     * </ul>
     *
     * @param adUnit Declaration of ad unit to get a bid from
     * @return a valid bid that may be displayed or <code>null</code> that should be ignored
     */
    @Nullable
    Slot getBidForAdUnitAndPrefetch(@Nullable AdUnit adUnit) {
        if (killSwitchEngaged()) {
            return null;
        }
        CacheAdUnit cacheAdUnit = adUnitMapper.convertValidAdUnit(adUnit);
        if (cacheAdUnit == null) {
            Log.e(TAG, "Valid AdUnit is required.");
            return null;
        }

        Slot peekSlot = cache.peekAdUnit(cacheAdUnit);
        if (peekSlot == null) {
            // If no matching bid response is found
            fetch(cacheAdUnit);
            return null;
        }

        double cpm = (peekSlot.getCpmAsNumber() == null ? 0.0 : peekSlot.getCpmAsNumber());
        long ttl = peekSlot.getTtl();
        long expiryTimeMillis = ttl * SECOND_TO_MILLI + peekSlot.getTimeOfDownload();

        boolean isNotExpired = expiryTimeMillis > clock.getCurrentTimeInMillis();
        boolean isValidBid = (cpm > 0) && (ttl > 0);
        boolean isSilentBid = (cpm == 0) && (ttl > 0);

        if (isSilentBid && isNotExpired) {
            return null;
        }

        cache.remove(cacheAdUnit);
        fetch(cacheAdUnit);

        if (isValidBid && isNotExpired) {
            return peekSlot;
        }
        return null;
    }


    private void checkAndReflect(Object object, String fieldName, String enrichmentKey) {
        if (!TextUtils.isEmpty(fieldName)) {
            ReflectionUtil.callMethodOnObject(object, "addCustomTargeting", enrichmentKey,
                    deviceUtil.createDfpCompatibleString(fieldName));
        }
    }

    @Override
    public void setCacheAdUnits(@NonNull List<Slot> slots) {
        long instant = clock.getCurrentTimeInMillis();
        for (Slot slot : slots) {
            if (slot.isValid()) {
                boolean isImmediateBid = slot.getCpmAsNumber() > 0 && slot.getTtl() == 0;
                if (isImmediateBid) {
                    slot.setTtl(DEFAULT_TTL_IN_SECONDS);
                }

                slot.setTimeOfDownload(instant);
                cache.add(slot);
            }
        }
    }

    @Override
    public void refreshConfig(@NonNull JSONObject configJSONObject) {
        config.refreshConfig(configJSONObject);
    }

    @Override
    public void setTimeToNextCall(int seconds) {
        if (seconds > 0) {
            this.cdbTimeToNextCall = clock.getCurrentTimeInMillis() + seconds * 1000;
        }
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

    @NonNull
    public BidResponse getBidForInhouseMediation(AdUnit adUnit) {
        Slot slot = this.getBidForAdUnitAndPrefetch(adUnit);
        if (slot == null) {
            return new BidResponse();
        }

        TokenValue tokenValue = new TokenValue(
            slot.getTimeOfDownload(),
            slot.getTtl(),
            slot.getDisplayUrl(),
            adUnit.getAdUnitType(),
            clock
        );

        double price = slot.getCpmAsNumber();
        return new BidResponse(price, tokenCache.add(tokenValue, adUnit), true);

    }

    /**
     * This method is called back after the "useragent" is fetched
     *
     * @param adUnits list of ad units to prefetch
     */
    public void prefetch(@NonNull List<AdUnit> adUnits) {
        List<CacheAdUnit> cacheAdUnits = adUnitMapper.convertValidAdUnits(adUnits);

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
