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

import static com.criteo.publisher.TestAdUnits.BANNER_320_50;
import static com.criteo.publisher.TestAdUnits.BANNER_UNKNOWN;
import static com.criteo.publisher.TestAdUnits.INTERSTITIAL;
import static com.criteo.publisher.TestAdUnits.INTERSTITIAL_UNKNOWN;
import static com.criteo.publisher.TestAdUnits.INTERSTITIAL_VIDEO;
import static com.criteo.publisher.TestAdUnits.NATIVE;
import static com.criteo.publisher.TestAdUnits.NATIVE_UNKNOWN;
import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.NonNull;
import com.criteo.publisher.StubConstants;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.csm.MetricRequest;
import com.criteo.publisher.logging.LogMessage;
import com.criteo.publisher.logging.RemoteLogRecords;
import com.criteo.publisher.logging.RemoteLogRecordsFactory;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Config.DefaultConfig;
import com.criteo.publisher.model.RemoteConfigRequest;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import com.criteo.publisher.model.RemoteConfigResponse;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.DeviceUtil;
import java.util.List;
import javax.inject.Inject;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class PubSdkApiIntegrationTest {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private int senderId;
  private int limitedAdTracking;
  private String gaid;
  private String eventType;
  private String appId;

  @Inject
  private Context context;

  @SpyBean
  private DeviceUtil deviceUtil;

  @Inject
  private AdUnitMapper adUnitMapper;

  @Inject
  private CdbRequestFactory cdbRequestFactory;

  @Inject
  private PubSdkApi api;

  @Inject
  private UserPrivacyUtil userPrivacyUtil;

  @Inject
  private RemoteConfigRequestFactory remoteConfigRequestFactory;

  @Inject
  private RemoteLogRecordsFactory remoteLogRecordsFactory;

  @Before
  public void setup() {
    appId = context.getApplicationContext().getPackageName();
    senderId = 2379;
    limitedAdTracking = 0;
    gaid = "021a86de-ef82-4f69-867b-61ca66688c9c";
    eventType = "Launch";
  }

  @After
  public void tearDown() {
    cleanupTcf1();
    cleanupTcf2();
  }

  @Test
  public void loadConfig_GivenNotConfiguredDimensions_ReturnDefaultValues() throws Exception {
    // The app represented by the android tests is not (and should not) be configured.
    RemoteConfigRequest request = remoteConfigRequestFactory.createRequest();

    RemoteConfigResponse response = api.loadConfig(request);

    assertThat(response).isEqualTo(defaultRemoteConfigResponse());
  }

  @Test
  public void postLogs_GivenRemoteLogs_ReturnInSuccess() throws Exception {
    RemoteLogRecords logRecords1 = remoteLogRecordsFactory.createLogRecords(new LogMessage(
        Log.INFO,
        "dummy message 1",
        null,
        null
    ));

    RemoteLogRecords logRecords2 = remoteLogRecordsFactory.createLogRecords(new LogMessage(
        Log.WARN,
        "dummy message 2",
        new Exception(),
        "dummyLogId"
    ));

    api.postLogs(asList(logRecords1, logRecords2));

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void postCsm_GivenMetric_ReturnInSuccess() throws Exception {
    MetricRequest request = mock(MetricRequest.class);

    api.postCsm(request);

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void postAppEvent_GivenNonNullGaid_ReturnInSuccess() throws Exception {
    gaid = null;

    api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        "fakeConsentData"
    );

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void postAppEvent_GivenNullGaid_ReturnInSuccess() throws Exception {
    api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        "fakeConsentData"
    );

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void loadCdb_GivenGeneratedRequest_ReturnInSuccess() throws Exception {
    CacheAdUnit adUnit = new CacheAdUnit(new AdSize(1, 2), "ad1", CRITEO_BANNER);

    CdbRequest request = cdbRequestFactory.createRequest(singletonList(adUnit), new ContextData());

    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertNotNull(response);
  }

  @Test
  public void loadCdb_GivenValidBannerAdUnit_ReturnBid() throws Exception {
    CacheAdUnit validAdUnit = adUnitMapper.map(BANNER_320_50);
    CdbRequest request = cdbRequestFactory.createRequest(singletonList(validAdUnit), new ContextData());

    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertThat(response.getTimeToNextCall()).isZero();
    assertThat(response.getSlots()).hasSize(1).allSatisfy(slot -> {
      assertThat(slot.getImpressionId()).isNotNull();
      assertThat(slot.getZoneId()).isNotNull();
      assertThat(slot.isNative()).isFalse();
      assertThat(slot.getPlacementId()).isEqualTo(BANNER_320_50.getAdUnitId());
      assertThat(slot.getCpm()).isNotEmpty();
      assertThat(slot.getWidth()).isEqualTo(320);
      assertThat(slot.getHeight()).isEqualTo(50);
      assertThat(slot.getCurrency()).isNotEmpty();
      assertThat(slot.getTtlInSeconds()).isEqualTo(3600);
      assertThat(slot.getDisplayUrl()).isNotEmpty().matches(StubConstants.STUB_DISPLAY_URL);
      assertThat(slot.getNativeAssets()).isNull();
      assertThat(slot.isValid()).isTrue();
    });
  }

  @Test
  public void loadCdb_GivenValidInterstitialAdUnit_ReturnBid() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(42, 1337));

    CacheAdUnit validAdUnit = adUnitMapper.map(INTERSTITIAL);
    CdbRequest request = cdbRequestFactory.createRequest(singletonList(validAdUnit), new ContextData());

    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertThat(response.getTimeToNextCall()).isZero();
    assertThat(response.getSlots()).hasSize(1).allSatisfy(slot -> {
      assertThat(slot.getImpressionId()).isNotNull();
      assertThat(slot.getZoneId()).isNotNull();
      assertThat(slot.isNative()).isFalse();
      assertThat(slot.getPlacementId()).isEqualTo(INTERSTITIAL.getAdUnitId());
      assertThat(slot.getCpm()).isNotEmpty();
      assertThat(slot.getWidth()).isEqualTo(42);
      assertThat(slot.getHeight()).isEqualTo(1337);
      assertThat(slot.getCurrency()).isNotEmpty();
      assertThat(slot.getTtlInSeconds()).isEqualTo(3600);
      assertThat(slot.getDisplayUrl()).isNotEmpty().matches(StubConstants.STUB_DISPLAY_URL);
      assertThat(slot.getNativeAssets()).isNull();
      assertThat(slot.isValid()).isTrue();
      assertThat(slot.isVideo()).isFalse();
    });
  }

  @Test
  public void loadCdb_GivenValidInterstitialVideoAdUnit_ReturnBid() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(42, 1337));

    CacheAdUnit validAdUnit = adUnitMapper.map(INTERSTITIAL_VIDEO);
    CdbRequest request = cdbRequestFactory.createRequest(singletonList(validAdUnit), new ContextData());

    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertThat(response.getTimeToNextCall()).isZero();
    assertThat(response.getSlots()).hasSize(1).allSatisfy(slot -> {
      assertThat(slot.getImpressionId()).isNotNull();
      assertThat(slot.getZoneId()).isNotNull();
      assertThat(slot.isNative()).isFalse();
      assertThat(slot.getPlacementId()).isEqualTo(INTERSTITIAL_VIDEO.getAdUnitId());
      assertThat(slot.getCpm()).isNotEmpty();
      assertThat(slot.getWidth()).isEqualTo(42);
      assertThat(slot.getHeight()).isEqualTo(1337);
      assertThat(slot.getCurrency()).isNotEmpty();
      assertThat(slot.getTtlInSeconds()).isEqualTo(3600);
      assertThat(slot.getDisplayUrl()).isNotEmpty().matches(StubConstants.STUB_VAST_DISPLAY_URL);
      assertThat(slot.getNativeAssets()).isNull();
      assertThat(slot.isValid()).isTrue();
      assertThat(slot.isVideo()).isTrue();
    });
  }

  @Test
  public void loadCdb_GivenValidNativeAdUnit_ReturnBid() throws Exception {
    CacheAdUnit validAdUnit = adUnitMapper.map(NATIVE);
    CdbRequest request = cdbRequestFactory.createRequest(singletonList(validAdUnit), new ContextData());

    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertThat(response.getTimeToNextCall()).isZero();
    assertThat(response.getSlots()).hasSize(1).allSatisfy(slot -> {
      assertThat(slot.getImpressionId()).isNotNull();
      assertThat(slot.getZoneId()).isNotNull();
      assertThat(slot.isNative()).isTrue();
      assertThat(slot.getPlacementId()).isEqualTo(NATIVE.getAdUnitId());
      assertThat(slot.getCpm()).isNotEmpty();
      assertThat(slot.getWidth()).isEqualTo(2);
      assertThat(slot.getHeight()).isEqualTo(2);
      assertThat(slot.getCurrency()).isNotEmpty();
      assertThat(slot.getTtlInSeconds()).isEqualTo(3600);
      assertThat(slot.getDisplayUrl()).isNull();
      assertThat(slot.getNativeAssets()).isEqualTo(StubConstants.STUB_NATIVE_ASSETS);
      assertThat(slot.isValid()).isTrue();
    });
  }

  @Test
  public void loadCdb_GivenMultipleValidAdUnits_ReturnBids() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(42, 1337));

    List<CacheAdUnit> validAdUnits = adUnitMapper.mapToChunks(asList(
        BANNER_320_50,
        INTERSTITIAL,
        NATIVE
    )).get(0);

    CdbRequest request = cdbRequestFactory.createRequest(validAdUnits, new ContextData());
    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertThat(validAdUnits).hasSize(3);
    assertThat(response.getSlots()).hasSize(3);
  }

  @Test
  public void loadCdb_GivenMultipleInvalidAdUnits_ReturnNoBids() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(42, 1337));

    List<CacheAdUnit> validAdUnits = adUnitMapper.mapToChunks(asList(
        BANNER_UNKNOWN,
        INTERSTITIAL_UNKNOWN,
        NATIVE_UNKNOWN
    )).get(0);

    CdbRequest request = cdbRequestFactory.createRequest(validAdUnits, new ContextData());
    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertThat(validAdUnits).hasSize(3);
    assertThat(response.getSlots()).hasSize(0);
  }

  @Test
  public void testPostAppEvent_WhenUsingEmptyGdprData() throws Exception {
    // Given
    setupGdprDataWithTcf2(
        "-1",
        ""
    );

    setupGdprDataWithTcf1(
        "",
        ""
    );

    // When
    JSONObject jsonObject = api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        "fakeConsentData"
    );

    // Then
    assertNotNull(jsonObject);
  }

  private void setupGdprDataWithTcf1(String gdprApplies, String tcString) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("IABConsent_SubjectToGDPR", gdprApplies);
    editor.putString("IABConsent_ConsentString", tcString);
    editor.apply();
  }

  private void setupGdprDataWithTcf2(String subjectToGdpr, String consentString) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putInt("IABTCF_gdprApplies", Integer.valueOf(subjectToGdpr));
    editor.putString("IABTCF_TCString", consentString);
    editor.apply();
  }

  private void cleanupTcf1() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.remove("IABConsent_ConsentString");
    editor.remove("IABConsent_SubjectToGDPR");
    editor.apply();
  }

  private void cleanupTcf2() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.remove("IABTCF_gdprApplies");
    editor.remove("IABTCF_TCString");
    editor.apply();
  }

  @NonNull
  private RemoteConfigResponse defaultRemoteConfigResponse() {
    return RemoteConfigResponse.create(
        DefaultConfig.KILL_SWITCH,
        DefaultConfig.DISPLAY_URL_MACRO,
        DefaultConfig.AD_TAG_URL_MODE,
        DefaultConfig.AD_TAG_DATA_MACRO,
        DefaultConfig.AD_TAG_DATA_MODE,
        DefaultConfig.CSM_ENABLED,
        DefaultConfig.LIVE_BIDDING_ENABLED,
        DefaultConfig.LIVE_BIDDING_TIME_BUDGET_IN_MILLIS,
        DefaultConfig.PREFETCH_ON_INIT_ENABLED,
        DefaultConfig.REMOTE_LOG_LEVEL
    );
  }
}
