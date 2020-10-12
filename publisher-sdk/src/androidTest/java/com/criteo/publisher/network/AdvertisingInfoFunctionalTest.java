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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.util.AdvertisingInfo;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

/**
 * This test file is purposefully located within the <code>com.criteo.publisher.network</code>
 * package as it needs to access {@link PubSdkApi#loadCdb(CdbRequest, String)} method.
 */
public class AdvertisingInfoFunctionalTest {

  private static final String FAKE_DEVICE_ID = "FAKE_DEVICE_ID";
  private static final String DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000";
  private final BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", new AdSize(1, 2));

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @MockBean
  private AdvertisingInfo advertisingInfo;

  @SpyBean
  private PubSdkApi pubSdkApi;

  @Inject
  private Context context;

  @Before
  public void setUp() throws  Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testBearcatCall_LimitedGAID() throws Exception {
    when(advertisingInfo.getAdvertisingId()).thenReturn(DEVICE_ID_LIMITED);

    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    CdbRequest cdb = cdbArgumentCaptor.getValue();

    assertEquals(DEVICE_ID_LIMITED, fetchDeviceIdSentInCdbRequest(cdb));
  }

  @Test
  public void testBearcatCall_NotEmptyGAID() throws Exception {
    when(advertisingInfo.getAdvertisingId()).thenReturn(FAKE_DEVICE_ID);

    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);

    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));
    CdbRequest cdb = cdbArgumentCaptor.getValue();

    assertEquals(FAKE_DEVICE_ID, fetchDeviceIdSentInCdbRequest(cdb));
  }

  @Test
  public void testStandaloneBannerRequest_LimitedGAID() throws Exception {
    when(advertisingInfo.getAdvertisingId()).thenReturn(DEVICE_ID_LIMITED);

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
  public void testStandaloneBannerRequest_NonEmptyGAID() throws Exception {
    when(advertisingInfo.getAdvertisingId()).thenReturn(FAKE_DEVICE_ID);

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

  private String fetchDeviceIdSentInCdbRequest(CdbRequest cdb) {
    return cdb.getUser().deviceId();
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }
}
