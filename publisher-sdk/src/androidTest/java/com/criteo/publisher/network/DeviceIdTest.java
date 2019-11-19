package com.criteo.publisher.network;

import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.ThreadingUtil;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Cdb;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * This test file is purposefully located within the <code>com.criteo.publisher.network</code>
 * package as it needs to access {@link PubSdkApi#loadCdb(Context, Cdb, String)} method.
 */
public class DeviceIdTest {
  private static final String FAKE_DEVICE_ID = "FAKE_DEVICE_ID";
  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";
  private final BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", new AdSize(1, 2));

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  @Mock
  private AdvertisingInfo advertisingInfo;

  @Mock
  private PubSdkApi pubSdkApi;

  private Context context;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(dependencyProvider.providePubSdkApi()).thenReturn(pubSdkApi);
    when(dependencyProvider.provideAdvertisingInfo()).thenReturn(advertisingInfo);

    context = InstrumentationRegistry.getContext();
  }

  @Test
  public void testBearcatCall_EmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    CriteoUtil.givenInitializedCriteo();

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    assertEquals("", fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testBearcatCall_EmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    CriteoUtil.givenInitializedCriteo();

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testBearcatCall_NotEmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    CriteoUtil.givenInitializedCriteo();

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    Cdb cdb = cdbArgumentCaptor.getValue();

    assertEquals(FAKE_DEVICE_ID, fetchDeviceIdSentInCdbRequest(cdb));
  }

  @Test
  public void testBearcatCall_NotEmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    CriteoUtil.givenInitializedCriteo();

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    Cdb cdb = cdbArgumentCaptor.getValue();

    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdb));
  }

  @Test
  public void testStandaloneBannerRequest_EmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    CriteoUtil.givenInitializedCriteo();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi, times(2)).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    assertEquals("", fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testStandaloneBannerRequest_NonEmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    CriteoUtil.givenInitializedCriteo();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi, times(2)).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(FAKE_DEVICE_ID, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testStandaloneBannerRequest_EmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    CriteoUtil.givenInitializedCriteo();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi, times(2)).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testStandaloneBannerRequest_NonEmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    CriteoUtil.givenInitializedCriteo();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    ThreadingUtil.waitForMockedBid();

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);

    verify(pubSdkApi, times(2)).loadCdb(any(Context.class), cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  private String fetchDeviceIdSentInCdbRequest(Cdb cdb) throws Exception {
    JSONObject cdbJSONObject = cdb.toJson();
    JSONObject userJSONObject = (JSONObject) cdbJSONObject.get("user");
    return (String) userJSONObject.get("deviceId");
  }
}
