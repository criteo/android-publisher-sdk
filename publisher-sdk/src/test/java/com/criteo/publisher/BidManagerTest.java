package com.criteo.publisher;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

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
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BidManagerTest {
    private String adUnitId = "someAdUnit";
    private AdSize adSize = new AdSize(320, 50);
    private AdUnit adUnit;
    private ArrayList<CacheAdUnit> cacheAdUnits;
    private Context context;
    private Publisher publisher;
    private User user;
    private SdkCache sdkCache;
    private DeviceInfo deviceInfo;
    private TokenCache tokenCache = null;
    private Slot testSlot;

    @Before
    public void setup() {
        adUnit = new BannerAdUnit(adUnitId, adSize);

        cacheAdUnits = new ArrayList<>();
        CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId);
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
                    "            \"placementId\": \""+adUnitId+"\",\n" +
                    "            \"cpm\": \"0.10\",\n" +
                    "            \"currency\": \"USD\",\n" +
                    "            \"width\": 320,\n" +
                    "            \"height\": 50,\n" +
                    "            \"ttl\": 3600,\n" +
                    "            \"displayUrl\": \"https://www.example.com/lone?par1=abcd\"\n" +
                    "        }");
        } catch (Exception ex) {
            // JSON threw
        }

        Assert.assertNotNull(slotJson);

        testSlot = new Slot(slotJson);
        when(this.sdkCache.peekAdUnit(adUnitId, adSize.getFormattedSize())).thenReturn(testSlot);
        when(this.sdkCache.getAdUnit(adUnitId, adSize.getFormattedSize())).thenReturn(testSlot);

        tokenCache = mock(TokenCache.class);
    }

    @Test
    public void testKillSwitchOnForHeaderBidding() {
        // setup
        Config config = mock(Config.class);
        when(config.isKillSwitch()).thenReturn(true);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache);
        bidManager.setConfig(config);

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
                , tokenCache, deviceInfo, user, sdkCache);
        bidManager.setConfig(config);

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
                , tokenCache, deviceInfo, user, sdkCache);
        bidManager.setConfig(config);

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
                , tokenCache, deviceInfo, user, sdkCache);
        bidManager.setConfig(config);

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
                , tokenCache, deviceInfo, user, sdkCache);
        bidManager.setConfig(config);

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

        when(this.sdkCache.peekAdUnit(adUnitId, adSize.getFormattedSize())).thenReturn(testSlot);
        when(this.sdkCache.getAdUnit(adUnitId, adSize.getFormattedSize())).thenReturn(testSlot);

        BidManager bidManager = new BidManager(context, publisher, cacheAdUnits
                , tokenCache, deviceInfo, user, sdkCache);
        bidManager.setConfig(config);

        BidResponse expectedResponse = new BidResponse(0.10d, new BidToken(UUID.randomUUID()), true);

        //test
        BidResponse bidResponse = bidManager.getBidForInhouseMediation(adUnit);
        Assert.assertNotNull(bidResponse);
        Assert.assertEquals(expectedResponse.getPrice(), bidResponse.getPrice(), 0.01);
        Assert.assertEquals(expectedResponse.isBidSuccess(), bidResponse.isBidSuccess());
        //can't compare the BidResponse.Token as it's a randomly generated UUID when inserting to token cache
    }

}
