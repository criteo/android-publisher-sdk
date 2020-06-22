/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.network;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.util.AdvertisingInfo;
import javax.inject.Inject;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * This test file is purposefully located within the <code>com.criteo.publisher.network</code>
 * package as it needs to access {@link PubSdkApi#loadCdb(CdbRequest, String)} method.
 */
public class DeviceIdTest {

  private static final String FAKE_DEVICE_ID = "FAKE_DEVICE_ID";
  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";
  private final BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", new AdSize(1, 2));

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private AdvertisingInfo advertisingInfo;

  @Mock
  private PubSdkApi pubSdkApi;

  @Inject
  private Context context;

  @Before
  public void setUp() throws  Exception {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(dependencyProvider.providePubSdkApi()).thenReturn(pubSdkApi);
    when(dependencyProvider.provideAdvertisingInfo()).thenReturn(advertisingInfo);
    mockedDependenciesRule.givenMockedRemoteConfigResponse(pubSdkApi);
  }

  @Test
  public void testBearcatCall_EmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    assertDeviceIdNotInCdbRequest(cdbArgumentCaptor.getValue());
  }

  @Test
  public void testBearcatCall_EmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testBearcatCall_NotEmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    CdbRequest cdb = cdbArgumentCaptor.getValue();

    assertEquals(FAKE_DEVICE_ID, fetchDeviceIdSentInCdbRequest(cdb));
  }

  @Test
  public void testBearcatCall_NotEmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    CdbRequest cdb = cdbArgumentCaptor.getValue();

    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdb));
  }

  @Test
  public void testStandaloneBannerRequest_EmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    givenInitializedCriteo();
    waitForIdleState();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    assertDeviceIdNotInCdbRequest(cdbArgumentCaptor.getValue());
  }

  @Test
  public void testStandaloneBannerRequest_NonEmptyGAID_TrackingAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(false);

    givenInitializedCriteo();
    waitForIdleState();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(FAKE_DEVICE_ID, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testStandaloneBannerRequest_EmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(null);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    givenInitializedCriteo();
    waitForIdleState();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  @Test
  public void testStandaloneBannerRequest_NonEmptyGAID_TrackingNotAllowed() throws Exception {
    when(advertisingInfo.getAdvertisingId(any(Context.class))).thenReturn(FAKE_DEVICE_ID);
    when(advertisingInfo.isLimitAdTrackingEnabled(any(Context.class))).thenReturn(true);

    givenInitializedCriteo();
    waitForIdleState();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerView.loadAd();
    });

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdbArgumentCaptor.getValue()));
  }

  private String fetchDeviceIdSentInCdbRequest(CdbRequest cdb) throws Exception {
    JSONObject userJsonObject = getUserJsonObject(cdb);
    return (String) userJsonObject.get("deviceId");
  }

  private void assertDeviceIdNotInCdbRequest(CdbRequest cdb) throws Exception {
    JSONObject userJsonObject = getUserJsonObject(cdb);
    assertFalse(userJsonObject.has("deviceId"));
  }

  private JSONObject getUserJsonObject(CdbRequest cdb) throws Exception {
    JSONObject cdbJsonObject = cdb.toJson();
    JSONObject userJsonObject = (JSONObject) cdbJsonObject.get("user");
    return userJsonObject;
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }
}
