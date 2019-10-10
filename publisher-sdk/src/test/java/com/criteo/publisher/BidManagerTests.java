package com.criteo.publisher;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.CdbDownloadTask;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BidManagerTests {

    private String adUnitId = "someAdUnit";
    private AdSize adSize = new AdSize(320, 50);
    private AdUnit adUnit;
    private String cpm = "0.10";
    private ArrayList<CacheAdUnit> cacheAdUnits;
    private Context context;
    private String displayUrl = "https://www.example.com/lone?par1=abcd";
    private Publisher publisher;
    private User user;
    private SdkCache sdkCache;
    private DeviceInfo deviceInfo;
    private TokenCache tokenCache = null;
    private Slot testSlot;
    @Mock
    private Hashtable<CacheAdUnit, CdbDownloadTask> placementsWithCdbTasks;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        adUnit = new BannerAdUnit(adUnitId, adSize);

        cacheAdUnits = new ArrayList<>();
        CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId, CRITEO_BANNER);
        cacheAdUnits.add(cAdUnit);

        context = mock(Context.class);
        when(context.getPackageName()).thenReturn("TestThisPackage");
        Resources resources = mock(Resources.class);
        when(context.getResources()).thenReturn(resources);
        Configuration configuration = mock(Configuration.class);
        // Configuration.ORIENTATION_PORTRAIT is 1
        configuration.orientation = 1;
        when(resources.getConfiguration()).thenReturn(configuration);

        publisher = new Publisher(context, "unitPublisherId");
        user = mock(User.class);
        sdkCache = mock(SdkCache.class);

        deviceInfo = mock(DeviceInfo.class);
        when(deviceInfo.getUserAgent()).thenReturn("Some fun user-agent that is probably webkit based 10.3");

        JSONObject slotJson = null;
        try {
            slotJson = new JSONObject("{\n" +
                    "            \"placementId\": \"" + adUnitId + "\",\n" +
                    "            \"cpm\": \"" + cpm + "\",\n" +
                    "            \"currency\": \"USD\",\n" +
                    "            \"width\": " + adSize.getWidth() + ",\n" +
                    "            \"height\": " + adSize.getHeight() + ",\n" +
                    "            \"ttl\": 3600,\n" +
                    "            \"displayUrl\": \"" + displayUrl + "\"\n" +
                    "        }");
        } catch (Exception ex) {
            // JSON threw
        }

        Assert.assertNotNull(slotJson);

        testSlot = new Slot(slotJson);
        when(this.sdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);
        when(this.sdkCache.getAdUnit(cAdUnit)).thenReturn(testSlot);

        tokenCache = mock(TokenCache.class);
    }

    @Test
    public void testKillSwitchOnForHeaderBidding() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(true);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        bidManager.enrichBid(builder, adUnit);

        PublisherAdRequest adRequest = builder.build();
        Assert.assertFalse(adRequest.getKeywords().contains("crt_cpm"));
    }

    @Test
    @Ignore("DeviceUtil.createDfpCompatibleDisplayUrl has an android.Util.Base64.coder that's unavailable in unit tests")
    public void testKillSwitchOffForHeaderBidding() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(false);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        bidManager.enrichBid(builder, adUnit);

        PublisherAdRequest adRequest = builder.build();
        Assert.assertTrue(adRequest.getKeywords().contains("crt_cpm"));
        Assert.assertTrue(adRequest.getKeywords().contains("crt_displayUrl"));
    }

    @Test
    public void testKillSwitchOnForStandAlone() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(true);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        //test
        Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
        Assert.assertNull(slot);
    }

    @Test
    public void testKillSwitchOffForStandAlone() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(false);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        //test
        Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
        Assert.assertNotNull(slot);
        Assert.assertEquals(testSlot, slot);
    }

    @Test
    public void testKillSwitchOnForMediation() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(true);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        BidResponse expectedResponse = new BidResponse();

        //test
        BidResponse bidResponse = bidManager.getBidForInhouseMediation(adUnit);
        Assert.assertNotNull(bidResponse);
        Assert.assertEquals(expectedResponse.getPrice(), bidResponse.getPrice(), 0.01);
        Assert.assertEquals(expectedResponse.isBidSuccess(), bidResponse.isBidSuccess());
        //can't compare the BidResponse.Token as it's a randomly generated UUID when inserting to token cache
    }

    @Test
    public void testKillSwitchOffForMediation() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(false);

        CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId, CRITEO_BANNER);
        when(this.sdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);
        when(this.sdkCache.getAdUnit(cAdUnit)).thenReturn(testSlot);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        BidResponse expectedResponse = new BidResponse(0.10d,
                new BidToken(UUID.randomUUID(), new BannerAdUnit("banneradUnitId1", new AdSize(320, 50))), true);

        //test
        BidResponse bidResponse = bidManager.getBidForInhouseMediation(adUnit);
        Assert.assertNotNull(bidResponse);
        Assert.assertEquals(expectedResponse.getPrice(), bidResponse.getPrice(), 0.01);
        Assert.assertEquals(expectedResponse.isBidSuccess(), bidResponse.isBidSuccess());
        //can't compare the BidResponse.Token as it's a randomly generated UUID when inserting to token cache
    }

    @Test
    public void testBidsAreNotSetOnNonBiddableObjects()
    {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(false);

        CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId, CRITEO_BANNER);
        when(this.sdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);
        when(this.sdkCache.getAdUnit(cAdUnit)).thenReturn(testSlot);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        BannerAdUnit bannerAdUnit = new BannerAdUnit(adUnitId, new AdSize(320,50));

        // Test null does not crash
        bidManager.enrichBid(null, bannerAdUnit);

        // Test set
        Set<String> set = new HashSet<>();
        bidManager.enrichBid(set, bannerAdUnit);

        Assert.assertEquals(0, set.size());

        // Test string
        String someString = "abcd123";
        bidManager.enrichBid(someString, bannerAdUnit);

        // Test Object
        Object someObject = new Object();
        bidManager.enrichBid(someObject, bannerAdUnit);
    }

    @Test
    public void testSetBidsOnMap()
    {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(false);

        CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId, CRITEO_BANNER);
        when(this.sdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);
        when(this.sdkCache.getAdUnit(cAdUnit)).thenReturn(testSlot);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache, config, placementsWithCdbTasks);

        BannerAdUnit bannerAdUnit = new BannerAdUnit(adUnitId, new AdSize(320,50));

        // Test Map
        Map<String,String> map = new HashMap<>();
        bidManager.enrichBid(map, bannerAdUnit);

        Assert.assertEquals(2, map.size());
        Assert.assertEquals(cpm, map.get("crt_cpm"));
        Assert.assertEquals(displayUrl, map.get("crt_displayUrl"));

        // Test Dictionary
        Dictionary<String, String> dict = new Hashtable<>();
        bidManager.enrichBid(dict, bannerAdUnit);

        Assert.assertEquals(2, dict.size());
        Assert.assertEquals(cpm, dict.get("crt_cpm"));
        Assert.assertEquals(displayUrl, dict.get("crt_displayUrl"));

        // Test nested custom class that implements map via a custom interface
        SpecialMap specialHashMap = new SpecialHashMap();
        bidManager.enrichBid(specialHashMap, bannerAdUnit);

        Assert.assertEquals(2, specialHashMap.size());
        Assert.assertEquals(cpm, specialHashMap.get("crt_cpm"));
        Assert.assertEquals(displayUrl, specialHashMap.get("crt_displayUrl"));
    }

    private interface SpecialMap extends Map
    {
    }

    private class SpecialHashMap extends HashMap implements SpecialMap
    {
    }

}
