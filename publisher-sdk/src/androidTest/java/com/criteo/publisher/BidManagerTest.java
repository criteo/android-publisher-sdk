package com.criteo.publisher;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.UiThreadTest;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.NativeAssets;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.CdbDownloadTask;
import com.criteo.publisher.network.PubSdkApi;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class BidManagerTest {
    @Rule
    public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

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

    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";
    private static final String CRITEO_PUBLISHER_ID = "1000";
    private Context context;
    private Publisher publisher;
    private User user;
    private SdkCache sdkCache;
    private Config config;

    @Mock
    private TokenCache tokenCache;

    @Mock
    private DeviceInfo deviceInfo;

    @Mock
    private SdkCache mockSdkCache;

    @Mock
    private AndroidUtil androidUtil;

    @Mock
    private AdvertisingInfo advertisingInfo;

    @Mock
    private LoggingUtil loggingUtil;

    private Clock clock;

    private DeviceUtil deviceUtil;

    private UserPrivacyUtil userPrivacyUtil;

    private Hashtable<CacheAdUnit, CdbDownloadTask> placementsWithCdbTasks;

    private AdUnitMapper adUnitMapper;

    private PubSdkApi api;

    private Executor runOnUiThreadExecutor;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("CriteoCachedKillSwitch", false);
        editor.apply();
        publisher = new Publisher(context, CRITEO_PUBLISHER_ID);
        deviceUtil = new DeviceUtil(context, advertisingInfo);
        user = new User(deviceUtil);
        sdkCache = new SdkCache(deviceUtil);
        config = new Config(context);
        placementsWithCdbTasks = new Hashtable<>();
        MockitoAnnotations.initMocks(this);
        clock = mockedDependenciesRule.getDependencyProvider().provideClock();
        userPrivacyUtil = mockedDependenciesRule.getDependencyProvider().provideUserPrivacyUtil(context);
        adUnitMapper = mockedDependenciesRule.getDependencyProvider().provideAdUnitMapper(context);
        api = mockedDependenciesRule.getDependencyProvider().providePubSdkApi(context.getApplicationContext());
        runOnUiThreadExecutor = mockedDependenciesRule.getDependencyProvider().provideRunOnUiThreadExecutor();
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
        assertNull(request.getCustomTargeting().getString(DFP_CRT_DISPLAY_URL));
    }

    @Test
    @UiThreadTest
    public void testPlacementAdditionInFetch() {
        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        AdSize adSize = new AdSize(320, 50);
        BannerAdUnit bannerAdUnit = new BannerAdUnit("UniqueId", adSize);
        CacheAdUnit placementKey = new CacheAdUnit(adSize, bannerAdUnit.getAdUnitId(), AdUnitType.CRITEO_BANNER);
        manager.getBidForAdUnitAndPrefetch(bannerAdUnit);
        CdbDownloadTask cdbDownloadTask = placementsWithCdbTasks.get(placementKey);
        assertNotNull(cdbDownloadTask);
        Assert.assertEquals(AsyncTask.Status.RUNNING, cdbDownloadTask.getStatus());
    }

    @Test
    @UiThreadTest
    public void testPlacementAdditionInPrefetch() throws InterruptedException {
        AdSize adSize = new AdSize(320, 50);
        AdSize adSize_2 = new AdSize(300, 250);

        CacheAdUnit cacheAdUnit1 = new CacheAdUnit(adSize, "SampleBannerAdUnitId1", AdUnitType.CRITEO_BANNER);
        CacheAdUnit cacheAdUnit2 = new CacheAdUnit(adSize_2, "SampleBannerAdUnitId2", AdUnitType.CRITEO_BANNER);

        BannerAdUnit bannerAdUnit1 = new BannerAdUnit(cacheAdUnit1.getPlacementId(), cacheAdUnit1.getSize());
        BannerAdUnit bannerAdUnit2 = new BannerAdUnit(cacheAdUnit2.getPlacementId(), cacheAdUnit2.getSize());

        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );
        manager.prefetch(Arrays.asList(bannerAdUnit1, bannerAdUnit2));
        CdbDownloadTask cdbDownloadTask1 = placementsWithCdbTasks.get(cacheAdUnit1);
        CdbDownloadTask cdbDownloadTask2 = placementsWithCdbTasks.get(cacheAdUnit2);
        assertNotNull(cdbDownloadTask1);
        assertNotNull(cdbDownloadTask2);
        Assert.assertEquals(AsyncTask.Status.RUNNING, cdbDownloadTask1.getStatus());
        Assert.assertEquals(AsyncTask.Status.RUNNING, cdbDownloadTask2.getStatus());

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

        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        manager.setCacheAdUnits(slots);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest request = builder.build();
        assertNull(request.getCustomTargeting().getString(DFP_CRT_DISPLAY_URL));
    }

    @Test
    @UiThreadTest
    public void testPrefetch() {
        List<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit AdUnit = new BannerAdUnit("/140800857/Endeavour_320x50", new AdSize(320, 50));
        adUnits.add(AdUnit);
        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

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
        assertNull(request.getCustomTargeting().getString(DFP_CRT_DISPLAY_URL));
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
        assertNotNull(request.getCustomTargeting().getString(DFP_CRT_DISPLAY_URL));
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

        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        manager.enrichBid(builder, AdUnit);
        PublisherAdRequest request = builder.build();
        assertNull(request.getCustomTargeting().getString(DFP_CRT_DISPLAY_URL));
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
        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

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

        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

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

        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

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

        BidManager manager = new BidManager(
            publisher,
            new TokenCache(),
            new DeviceInfo(context, runOnUiThreadExecutor),
            user,
            sdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        manager.setCacheAdUnits(slots);
        BidResponse bidResponse = manager.getBidForInhouseMediation(adUnit);
        Assert.assertTrue(bidResponse.isBidSuccess());
        Assert.assertEquals(10.0d, bidResponse.getPrice(), 0.0);
    }


    @Test
    public void testDfpBannerInterstitialKeywordsReflection() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitchEnabled()).thenReturn(false);
        List<CacheAdUnit> cacheAdUnits = new ArrayList<>();
        CacheAdUnit cAdUnit = new CacheAdUnit(new AdSize(320, 50), "bannerAdunitId", AdUnitType.CRITEO_BANNER);
        cacheAdUnits.add(cAdUnit);

        JSONObject slotJson = null;
        BannerAdUnit bannerAdUnit = new BannerAdUnit("bannerAdunitId", new AdSize(320, 50));

        try {
            slotJson = new JSONObject("{\n" +
                    "            \"placementId\": \"" + bannerAdUnit + "\",\n" +
                    "            \"cpm\": \"0.10\",\n" +
                    "            \"currency\": \"USD\",\n" +
                    "            \"width\": 320,\n" +
                    "            \"height\": 50,\n" +
                    "            \"ttl\": 3600,\n" +
                    "            \"displayUrl\": \"https://www.example.com/lone?par1=abcd\"\n" +
                    "        }");
        } catch (Exception ex) {
            Assert.fail("Invalid json");
        }

        org.junit.Assert.assertNotNull(slotJson);

        Slot testSlot = new Slot(slotJson);
        testSlot.setTimeOfDownload(clock.getCurrentTimeInMillis());
        when(this.mockSdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);

        BidManager bidManager = new BidManager(
            publisher,
            tokenCache,
            deviceInfo,
            user,
            mockSdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        bidManager.enrichBid(builder, bannerAdUnit);

        PublisherAdRequest adRequest = builder.build();
        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_CPM));
        assertEquals("0.10", adRequest.getCustomTargeting().get(CRT_CPM));
        assertTrue(adRequest.getCustomTargeting().containsKey(DFP_CRT_DISPLAY_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://www.example.com/lone?par1=abcd"),
                adRequest.getCustomTargeting().get(DFP_CRT_DISPLAY_URL));

    }

    @Test
    public void testDfpNativeKeywordsReflection() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitchEnabled()).thenReturn(false);
        List<CacheAdUnit> cacheAdUnits = new ArrayList<>();
        CacheAdUnit cAdUnit = new CacheAdUnit(new AdSize(2, 2), "nativeAdunitId", AdUnitType.CRITEO_CUSTOM_NATIVE);
        cacheAdUnits.add(cAdUnit);

        JSONObject slotJson = null;
        NativeAdUnit nativeAdunitId = new NativeAdUnit("nativeAdunitId");

        try {
            slotJson = new JSONObject("{\n" +
                    "        \"placementId\": \"/140800857/Endeavour_Native\",\n" +
                    "        \"cpm\": \"0.04\",\n" +
                    "        \"currency\": \"USD\",\n" +
                    "        \"width\": 2,\n" +
                    "        \"height\": 2,\n" +
                    "        \"ttl\": 3600,\n" +
                    "        \"native\": {\n" +
                    "            \"products\": [{\n" +
                    "                \"title\": \"Stripe Pima Dress\",\n" +
                    "                \"description\": \"We're All About Comfort.\",\n" +
                    "                \"price\": \"$99\",\n" +
                    "                \"clickUrl\": \"https://cat.sv.us.criteo.com/delivery/ckn.php\",\n" +
                    "                \"callToAction\": \"Call to Action\",\n" +
                    "                \"image\": {\n" +
                    "                    \"url\": \"https://pix.us.criteo.net/img/img?\",\n" +
                    "                    \"height\": 400,\n" +
                    "                    \"width\": 400\n" +
                    "                }\n" +
                    "            }],\n" +
                    "            \"advertiser\": {\n" +
                    "                \"description\": \"The Company Store\",\n" +
                    "                \"domain\": \"thecompanystore.com\",\n" +
                    "                \"logo\": {\n" +
                    "                    \"url\": \"https://pix.us.criteo.net/img/img\",\n" +
                    "                    \"height\": 200,\n" +
                    "                    \"width\": 200\n" +
                    "                },\n" +
                    "                \"logoClickUrl\": \"https://cat.sv.us.criteo.com/delivery/ckn.php\"\n" +
                    "            },\n" +
                    "            \"privacy\": {\n" +
                    "                \"optoutClickUrl\": \"https://privacy.us.criteo.com/adcenter\",\n" +
                    "                \"optoutImageUrl\": \"https://static.criteo.net/flash/icon/nai_small.png\",\n" +
                    "                \"longLegalText\": \"Long Legal Text\"\n" +
                    "            },\n" +
                    "            \"impressionPixels\": [{\n" +
                    "                \"url\": \"https://cat.sv.us.criteo.com/delivery/lgn.php?\"},{\n" +
                    "                \"url\": \"https://dog.da.us.criteo.com/delivery/lgn.php?\"\n" +
                    "            }]\n" +
                    "        }\n" +
                    "        }");
        } catch (Exception ex) {
            Assert.fail("Invalid json");
        }

        org.junit.Assert.assertNotNull(slotJson);

        Slot testSlot = new Slot(slotJson);
        testSlot.setTimeOfDownload(clock.getCurrentTimeInMillis());
        when(this.mockSdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);

        BidManager bidManager = new BidManager(
            publisher,
            tokenCache,
            deviceInfo,
            user,
            mockSdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        bidManager.enrichBid(builder, nativeAdunitId);

        PublisherAdRequest adRequest = builder.build();
        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_CPM));
        assertEquals("0.04", adRequest.getCustomTargeting().get(CRT_CPM));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_TITLE));
        assertEquals(deviceUtil.createDfpCompatibleString("Stripe Pima Dress"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_TITLE));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_DESC));
        assertEquals(deviceUtil.createDfpCompatibleString("We're All About Comfort."),
                adRequest.getCustomTargeting().get(CRT_NATIVE_DESC));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PRICE));
        assertEquals(deviceUtil.createDfpCompatibleString("$99"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PRICE));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_CLICK_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://cat.sv.us.criteo.com/delivery/ckn.php"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_CLICK_URL));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_CTA));
        assertEquals(deviceUtil.createDfpCompatibleString("Call to Action"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_CTA));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_IMAGE_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://pix.us.criteo.net/img/img?"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_IMAGE_URL));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_ADV_NAME));
        assertEquals(deviceUtil.createDfpCompatibleString("The Company Store"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_ADV_NAME));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_ADV_DOMAIN));
        assertEquals(deviceUtil.createDfpCompatibleString("thecompanystore.com"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_ADV_DOMAIN));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_ADV_LOGO_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://pix.us.criteo.net/img/img"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_ADV_LOGO_URL));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_ADV_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://cat.sv.us.criteo.com/delivery/ckn.php"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_ADV_URL));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PR_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://privacy.us.criteo.com/adcenter"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PR_URL));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PR_IMAGE_URL));
        assertEquals(deviceUtil.createDfpCompatibleString("https://static.criteo.net/flash/icon/nai_small.png"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PR_IMAGE_URL));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PR_TEXT));
        assertEquals(deviceUtil.createDfpCompatibleString("Long Legal Text"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PR_TEXT));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PIXEL_URL + "0"));
        assertEquals(deviceUtil.createDfpCompatibleString("https://cat.sv.us.criteo.com/delivery/lgn.php?"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PIXEL_URL + "0"));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PIXEL_URL + "1"));
        assertEquals(deviceUtil.createDfpCompatibleString("https://dog.da.us.criteo.com/delivery/lgn.php?"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PIXEL_URL + "1"));

        assertTrue(adRequest.getCustomTargeting().containsKey(CRT_NATIVE_PIXEL_COUNT));
        assertEquals(("2"),
                adRequest.getCustomTargeting().get(CRT_NATIVE_PIXEL_COUNT));
    }

    /* This test is for https://jira.criteois.com/browse/EE-516
       when NativeAssets.products is null or NativeAssets.impressionpixels is null application was crashing.
       With this test . checking nativeProducts is null , impressionPixels is null and application not crashing.
       In prod slot.isValid() will reject the bids as it's value is checked upstream. This is to test the code that populates the keys.
    */
    @Test
    public void testEnrichNativeRequestWithNullProducts() {
        BannerAdUnit bannerAdUnit = new BannerAdUnit("BannerAdUnitId", new AdSize(320, 50));
        List<CacheAdUnit> adUnits = new ArrayList<>();
        CacheAdUnit cacheAdUnit = adUnitMapper.convertValidAdUnit(bannerAdUnit);
        adUnits.add(cacheAdUnit);

        BidManager bidManager = new BidManager(
            publisher,
            tokenCache,
            deviceInfo,
            user,
            mockSdkCache,
            placementsWithCdbTasks,
            config,
            deviceUtil,
            loggingUtil,
            clock,
            userPrivacyUtil,
            adUnitMapper,
            api
        );

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        Object object = builder;
        Slot slot = mock(Slot.class);
        NativeAssets nativeAssets = mock(NativeAssets.class);
        when(mockSdkCache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
        when(slot.isValid()).thenReturn(true);
        when(slot.isNative()).thenReturn(true);
        when(slot.getCpmAsNumber()).thenReturn(2.2);
        when(slot.getTtl()).thenReturn(10000);
        bidManager.enrichBid(object, bannerAdUnit);
        when(slot.getNativeAssets()).thenReturn(nativeAssets);
        Assert.assertNotNull(nativeAssets);
        Assert.assertNull(nativeAssets.nativeProducts);
        Assert.assertNull(nativeAssets.impressionPixels);
    }
}
