package com.criteo.publisher;

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.csm.MetricSendingQueueConsumer;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.network.BidRequestSender;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.DeviceUtil;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BidManagerTests {

  private String adUnitId = "someAdUnit";
  private AdSize adSize = new AdSize(320, 50);
  private AdUnit adUnit;
  private String cpm = "0.10";
  private String displayUrl = "https://www.example.com/lone?par1=abcd";
  private SdkCache sdkCache;
  private Slot testSlot;

  @Mock
  private DependencyProvider dependencyProvider;

  @Mock
  private Context context;

  @Mock
  private Config config;

  @Mock
  private AndroidUtil androidUtil;

  @Mock
  private DeviceUtil deviceUtil;

  @Mock
  private Clock clock;

  private AdUnitMapper adUnitMapper;

  @Mock
  private BidRequestSender bidRequestSender;

  @Mock
  private BidLifecycleListener bidLifecycleListener;

  @Mock
  private MetricSendingQueueConsumer metricSendingQueueConsumer;

  @Mock
  private InterstitialActivityHelper interstitialActivityHelper;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    adUnitMapper = new AdUnitMapper(androidUtil, deviceUtil);

    adUnit = new BannerAdUnit(adUnitId, adSize);

    CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId, CRITEO_BANNER);

    when(context.getPackageName()).thenReturn("TestThisPackage");

    // FIXME This seems useless because tests still works without.
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);

    sdkCache = mock(SdkCache.class);

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

    DependencyProvider.setInstance(dependencyProvider);
  }

  @After
  public void tearDown() {
    DependencyProvider.setInstance(null);
  }

  @Test
  public void testKillSwitchOnForStandAlone() {
    // setup
    when(config.isKillSwitchEnabled()).thenReturn(true);

    BidManager bidManager = createBidManager();

    //test
    Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    Assert.assertNull(slot);
  }

  @Test
  public void testKillSwitchOffForStandAlone() {
    // setup
    when(config.isKillSwitchEnabled()).thenReturn(false);

    BidManager bidManager = createBidManager();

    //test
    Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    Assert.assertNotNull(slot);
    Assert.assertEquals(testSlot, slot);
  }

  @Test
  public void testKillSwitchOnForMediation() {
    // setup
    when(config.isKillSwitchEnabled()).thenReturn(true);

    BidManager bidManager = createBidManager();
    InHouse inHouse = createInHouse(bidManager);

    BidResponse expectedResponse = new BidResponse();

    //test
    BidResponse bidResponse = inHouse.getBidResponse(adUnit);
    Assert.assertNotNull(bidResponse);
    Assert.assertEquals(expectedResponse.getPrice(), bidResponse.getPrice(), 0.01);
    Assert.assertEquals(expectedResponse.isBidSuccess(), bidResponse.isBidSuccess());
    //can't compare the BidResponse.Token as it's a randomly generated UUID when inserting to token cache
  }

  @Test
  public void testKillSwitchOffForMediation() {
    // setup
    when(config.isKillSwitchEnabled()).thenReturn(false);

    CacheAdUnit cAdUnit = new CacheAdUnit(adSize, adUnitId, CRITEO_BANNER);
    when(this.sdkCache.peekAdUnit(cAdUnit)).thenReturn(testSlot);

    BidManager bidManager = createBidManager();
    InHouse inHouse = createInHouse(bidManager);

    BidResponse expectedResponse = new BidResponse(0.10d,
        new BidToken(UUID.randomUUID(), new BannerAdUnit("banneradUnitId1", new AdSize(320, 50))),
        true);

    //test
    BidResponse bidResponse = inHouse.getBidResponse(adUnit);
    Assert.assertNotNull(bidResponse);
    Assert.assertEquals(expectedResponse.getPrice(), bidResponse.getPrice(), 0.01);
    Assert.assertEquals(expectedResponse.isBidSuccess(), bidResponse.isBidSuccess());
    //can't compare the BidResponse.Token as it's a randomly generated UUID when inserting to token cache
  }

  @NonNull
  private BidManager createBidManager() {
    return new BidManager(
        sdkCache,
        config,
        clock,
        adUnitMapper,
        bidRequestSender,
        bidLifecycleListener,
        metricSendingQueueConsumer
    );
  }

  @NonNull
  private InHouse createInHouse(BidManager bidManager) {
    return new InHouse(bidManager, mock(TokenCache.class), clock, interstitialActivityHelper);
  }

}
