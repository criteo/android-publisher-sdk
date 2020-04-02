package com.criteo.publisher.network;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.csm.MetricRequest;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.privacy.gdpr.GdprData;
import javax.inject.Inject;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
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
  private CdbRequestFactory cdbRequestFactory;
  private PubSdkApi api;
  private GdprData gdprData;
  private UserPrivacyUtil userPrivacyUtil;

  @Before
  public void setup() {
    appId = context.getApplicationContext().getPackageName();
    senderId = 2379;
    limitedAdTracking = 0;
    gaid = "021a86de-ef82-4f69-867b-61ca66688c9c";
    eventType = "Launch";

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    cdbRequestFactory = dependencyProvider.provideCdbRequestFactory();
    api = dependencyProvider.providePubSdkApi();
    userPrivacyUtil = mockedDependenciesRule.getDependencyProvider().provideUserPrivacyUtil();
  }

  @After
  public void tearDown() {
    cleanupTcf1();
    cleanupTcf2();
  }

  @Test
  public void postCsm_GivenMetric_ReturnInSuccess() throws Exception {
    MetricRequest request = mock(MetricRequest.class);

    api.postCsm(request);

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void postAppEvent_GivenNonNullGaid_ReturnInSuccess() {
    gaid = null;

    api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        gdprData
    );

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void postAppEvent_GivenNullGaid_ReturnInSuccess() {
    api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        gdprData
    );

    // nothing to assert, no thrown exception means success
  }

  @Test
  public void loadCdb_GivenGeneratedRequest_ReturnInSuccess() throws Exception {
    CacheAdUnit adUnit = new CacheAdUnit(new AdSize(1, 2), "ad1", CRITEO_BANNER);
    CdbRequest request = cdbRequestFactory.createRequest(singletonList(adUnit));

    CdbResponse response = api.loadCdb(request, "myUserAgent");

    assertNotNull(response);
  }

  @Test
  public void testGetGdprDataString_WhenUsingTcf2() {
    // Given
    setupGdprDataWithTcf2(
      "0",
      "ssds"
    );
    gdprData = userPrivacyUtil.getGdprData();

    // When
    String gdprString = api.getGdprDataStringBase64(gdprData);

    // Then

    // Value generated using: https://www.base64decode.org/ on:
    // {"consentData":"ssds","gdprApplies":false,"version":2}
    assertEquals(
        "eyJjb25zZW50RGF0YSI6InNzZHMiLCJnZHByQXBwbGllcyI6ZmFsc2UsInZlcnNpb24iOjJ9",
        gdprString
    );
  }

  @Test
  public void testPostAppEvent_WhenUsingEmptyGdprData() {
    // Given
    setupGdprDataWithTcf2(
        "-1",
        ""
    );

    setupGdprDataWithTcf1(
        "",
        ""
    );

    gdprData = userPrivacyUtil.getGdprData();

    // When
    JSONObject jsonObject = api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        gdprData
    );

    // Then
    assertNotNull(jsonObject);
  }

  @Test
  public void testGetGdprDataString_WhenUsingTcf1() {
    // Given
    setupGdprDataWithTcf1(
        "1",
        "ssds"
    );
    gdprData = userPrivacyUtil.getGdprData();

    // When

    // Value generated using: https://www.base64decode.org/ on:
    // {"consentData":"ssds","gdprApplies":true,"version":1}
    String gdprString = api.getGdprDataStringBase64(gdprData);

    // Then
    assertEquals(
        "eyJjb25zZW50RGF0YSI6InNzZHMiLCJnZHByQXBwbGllcyI6dHJ1ZSwidmVyc2lvbiI6MX0=",
        gdprString
    );

    gdprData = userPrivacyUtil.getGdprData();
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
}
