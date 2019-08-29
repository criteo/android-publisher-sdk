package com.criteo.publisher;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.UiThreadTest;
import android.util.Pair;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitHelper;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class BidManagerTest {

    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";
    private static final String CRITEO_PUBLISHER_ID = "1000";
    private Context context;
    private Publisher publisher;
    private User user;
    private SdkCache sdkCache;
    private Config config;

    @Mock
    private List<CacheAdUnit> mockCacheAdUnits;

    @Mock
    private Hashtable<Pair<String, String>, Boolean> placementsWithCdbTasks;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("CriteoCachedKillSwitch", false);
        editor.apply();
        publisher = new Publisher(context, CRITEO_PUBLISHER_ID);
        user = new User();
        sdkCache = new SdkCache();
        config = new Config(context);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @UiThreadTest
    public void testSilentMode() {
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));

        BidManager manager = getInitManager();
        manager.setTimeToNextCall(1000);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest.Builder builderUpdate = new PublisherAdRequest.Builder();
        manager.enrichBid(builderUpdate, AdUnit);
        PublisherAdRequest request = builderUpdate.build();
        assertNull(request.getCustomTargeting().getString("crt_displayUrl"));
    }

    @Test
    @UiThreadTest
    public void testPlacementAdditionInFetch() {
        BidManager manager = new BidManager(context, publisher, mockCacheAdUnits,
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        BannerAdUnit bannerAdUnit = new BannerAdUnit("UniqueId", new AdSize(320, 50));
        String formattedSize = "320x50";
        Pair<String, String> placementKey = new Pair<>(bannerAdUnit.getAdUnitId(),
                formattedSize);
        manager.getBidForAdUnitAndPrefetch(bannerAdUnit);
        Mockito.verify(placementsWithCdbTasks, Mockito.times(1)).put(placementKey, true);
    }

    @Test
    @UiThreadTest
    public void testPlacementAdditionInPrefetch() {
        List<CacheAdUnit> cacheAdUnits = new ArrayList();
        CacheAdUnit cacheAdUnit1 = new CacheAdUnit(new AdSize(320, 50), "SampleBannerAdUnitId1");
        CacheAdUnit cacheAdUnit2 = new CacheAdUnit(new AdSize(300, 250), "SampleBannerAdUnitId2");
        cacheAdUnits.add(cacheAdUnit1);
        cacheAdUnits.add(cacheAdUnit2);

        BidManager manager = new BidManager(context, publisher, cacheAdUnits,
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);

        String formattedSize1 = "320x50";
        Pair<String, String> placementKey1 = new Pair<>(cacheAdUnit1.getPlacementId(),
                formattedSize1);
        String formattedSize2 = "300x250";
        Pair<String, String> placementKey2 = new Pair<>(cacheAdUnit2.getPlacementId(),
                formattedSize2);

        manager.prefetch();
        Mockito.verify(placementsWithCdbTasks, Mockito.times(1)).put(placementKey1, true);
        Mockito.verify(placementsWithCdbTasks, Mockito.times(1)).put(placementKey2, true);
    }

    @Test
    @UiThreadTest
    public void testSilentModeSlotZeroTtlZeroCPM() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(AdUnit);
        Slot slot1 = new Slot();
        slot1.setPlacementId("/140800857/Endeavour_320x50");
        slot1.setHeight(50);
        slot1.setWidth(320);
        slot1.setCpm("0.0");
        slot1.setDisplayUrl(TEST_CREATIVE);
        slot1.setTtl(0);
        List<Slot> slots = new ArrayList<>();
        slots.add(slot1);

        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        manager.setCacheAdUnits(slots);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest request = builder.build();
        assertNull(request.getCustomTargeting().getString("crt_displayUrl"));
    }

    @Test
    @UiThreadTest
    public void testSilentModeSlotZeroCpmNonZeroTtl() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(AdUnit);
        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        List<Slot> slots = new ArrayList<>();
        Slot slot1 = new Slot();
        slot1.setPlacementId("/140800857/Endeavour_320x50");
        slot1.setHeight(50);
        slot1.setWidth(320);
        slot1.setDisplayUrl(TEST_CREATIVE);
        slot1.setCpm("0.0");
        slot1.setTimeOfDownload(System.currentTimeMillis());
        slot1.setTtl(3);
        slots.add(slot1);
        manager.setCacheAdUnits(slots);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest request = builder.build();
        assertNull(request.getCustomTargeting().getString("crt_displayUrl"));
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        manager.enrichBid(builder, AdUnit);
        request = builder.build();
        assertNotNull(request.getCustomTargeting().getString("crt_displayUrl"));
    }

    @Test
    @UiThreadTest
    public void testPrefetch() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(AdUnit);
        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        List<Slot> slots = new ArrayList<>();
        Slot slot1 = new Slot();
        slot1.setPlacementId("/140800857/Endeavour_320x50");
        slot1.setHeight(50);
        slot1.setWidth(320);
        slot1.setDisplayUrl(TEST_CREATIVE);
        slot1.setCpm("0");
        slot1.setTimeOfDownload(System.currentTimeMillis());
        slot1.setTtl(0);
        slots.add(slot1);
        manager.setCacheAdUnits(slots);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest request = builder.build();
        assertNull(request.getCustomTargeting().getString("crt_displayUrl"));
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //mocking response
        slots.clear();
        Slot slot2 = new Slot();
        slot2.setPlacementId("/140800857/Endeavour_320x50");
        slot2.setHeight(50);
        slot2.setWidth(320);
        slot2.setDisplayUrl(TEST_CREATIVE);
        slot2.setCpm("1");
        slot2.setTimeOfDownload(System.currentTimeMillis());
        slot2.setTtl(0);
        slots.add(slot2);
        manager.setCacheAdUnits(slots);
        manager.enrichBid(builder, AdUnit);
        request = builder.build();
        assertNotNull(request.getCustomTargeting().getString("crt_displayUrl"));


    }

    @Test
    @UiThreadTest
    public void testBidNoSilentMode() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(AdUnit);
        //mocking request
        Slot bannerSlot = new Slot();
        bannerSlot.setPlacementId("/140800857/Endeavour_320x50");
        bannerSlot.setHeight(50);
        bannerSlot.setWidth(320);
        bannerSlot.setCpm("1.2");
        bannerSlot.setDisplayUrl(TEST_CREATIVE);
        bannerSlot.setTtl(0);
        List<Slot> slots = new ArrayList<>();
        slots.add(bannerSlot);

        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest request = builder.build();
        assertNull(request.getCustomTargeting().getString("crt_displayUrl"));
    }

    //TODO test for getBidForAdUnitAndPrefetch , clear the cache and check whats happening

    private BidManager getInitManager() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(AdUnit);
        Slot bannerSlot = new Slot();
        bannerSlot.setPlacementId("/140800857/Endeavour_320x50");
        bannerSlot.setHeight(50);
        bannerSlot.setWidth(320);
        bannerSlot.setCpm("1.2");
        bannerSlot.setDisplayUrl(TEST_CREATIVE);
        bannerSlot.setTtl(0);

        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("/140800857/Endeavour_320x480");
        adUnits.add(interstitialAdUnit);

        Slot interstitialSlot = new Slot();
        interstitialSlot.setPlacementId("/140800857/Endeavour_Interstitial_320x480");
        interstitialSlot.setHeight(320);
        interstitialSlot.setWidth(480);
        interstitialSlot.setCpm("0.0");
        interstitialSlot.setDisplayUrl(TEST_CREATIVE);
        interstitialSlot.setTtl(0);

        List<Slot> slots = new ArrayList<>();
        slots.add(bannerSlot);
        slots.add(interstitialSlot);

        //initializing with adunits
        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        //mocking response by setting slots
        manager.setCacheAdUnits(slots);
        return manager;
    }

    @Test
    public void getBidForInhouseMediationWithNullSlot() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit adUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(adUnit);
        Slot slot1 = new Slot();
        List<Slot> slots = new ArrayList<>();
        slots.add(slot1);
        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        manager.setCacheAdUnits(slots);
        BidResponse bidResponse = manager.getBidForInhouseMediation(adUnit);
        Assert.assertFalse(bidResponse.isBidSuccess());
    }

    @Test
    public void getBidForInhouseMediationWithInvalidSlot() throws JSONException {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit adUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(adUnit);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cpm", "-10.0");
        Slot slot1 = new Slot(jsonObject);
        List<Slot> slots = new ArrayList<>();
        slots.add(slot1);
        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        manager.setCacheAdUnits(slots);
        BidResponse bidResponse = manager.getBidForInhouseMediation(adUnit);
        Assert.assertFalse(bidResponse.isBidSuccess());
    }


    @Test
    public void getBidForInhouseMediationWithSlot() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit adUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(adUnit);
        Slot slot1 = new Slot();
        slot1.setPlacementId("/140800857/Endeavour_320x50");
        slot1.setHeight(50);
        slot1.setWidth(320);
        slot1.setCpm("10.0");
        slot1.setTimeOfDownload(5);
        slot1.setTtl(1);
        slot1.setDisplayUrl(TEST_CREATIVE);
        List<Slot> slots = new ArrayList<>();
        slots.add(slot1);
        BidManager manager = new BidManager(context, publisher, AdUnitHelper.convertAdUnits(context, adUnits),
                new TokenCache(), new DeviceInfo(), user, sdkCache, config, placementsWithCdbTasks);
        manager.setCacheAdUnits(slots);
        BidResponse bidResponse = manager.getBidForInhouseMediation(adUnit);
        Assert.assertTrue(bidResponse.isBidSuccess());
        Assert.assertEquals(10.0d, bidResponse.getPrice(), 0.0);
    }

}
