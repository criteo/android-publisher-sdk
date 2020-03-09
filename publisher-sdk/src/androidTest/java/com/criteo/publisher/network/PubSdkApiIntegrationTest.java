package com.criteo.publisher.network;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.privacy.gdpr.GdprData;
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
  private Context context;
  private String appId;
  private PubSdkApi api;
  private GdprData gdprData;
  private UserPrivacyUtil userPrivacyUtil;

  @Before
  public void setup() {
    context = InstrumentationRegistry.getContext().getApplicationContext();
    appId = context.getApplicationContext().getPackageName();
    senderId = 2379;
    limitedAdTracking = 0;
    gaid = "021a86de-ef82-4f69-867b-61ca66688c9c";
    eventType = "Launch";
    api = mockedDependenciesRule.getDependencyProvider().providePubSdkApi();
    userPrivacyUtil = mockedDependenciesRule.getDependencyProvider().provideUserPrivacyUtil(context);
  }

  @After
  public void tearDown() {
    cleanupTcf1();
    cleanupTcf2();
  }

  @Test
  public void testPostAppEventWithNullGaid() {
    gaid = null;

    JSONObject object = api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        gdprData
    );

    assertNotNull(object);
  }

  @Test
  public void testPostAppEventWithGaid() {
    JSONObject object = api.postAppEvent(
        senderId,
        appId,
        gaid,
        eventType,
        limitedAdTracking,
        "",
        gdprData
    );

    assertNotNull(object);
  }

  @Test
  public void testGetGdprDataString_WhenUsingTcf2() {
    // Given
    setupGdprDataWithTcf2(
      "1",
      "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001",
      "ssds"
    );
    gdprData = userPrivacyUtil.getGdprData();

    // When
    String gdprString = api.getGdprDataStringBase64(gdprData);

    // Then

    // Value generated using: https://www.base64decode.org/ on:
    // {"consentGiven":true,"consentData":"ssds","gdprApplies":false,"version":2}
    assertEquals(
        "eyJjb25zZW50R2l2ZW4iOnRydWUsImNvbnNlbnREYXRhIjoic3NkcyIsImdkcHJBcHBsaWVzIjpmYWxzZSwidmVyc2lvbiI6Mn0=",
        gdprString
    );
  }

  @Test
  public void testPostAppEvent_WhenUsingEmptyGdprData() {
    // Given
    setupGdprDataWithTcf2(
        "",
        "",
        ""
    );

    setupGdprDataWithTcf1(
        "",
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
        "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001",
        "ssds"
    );
    gdprData = userPrivacyUtil.getGdprData();

    // When

    // Value generated using: https://www.base64decode.org/ on:
    // {"consentGiven":true,"consentData":"ssds","gdprApplies":true,"version":1}
    String gdprString = api.getGdprDataStringBase64(gdprData);

    // Then
    assertEquals(
        "eyJjb25zZW50R2l2ZW4iOnRydWUsImNvbnNlbnREYXRhIjoic3NkcyIsImdkcHJBcHBsaWVzIjp0cnVlLCJ2ZXJzaW9uIjoxfQ==",
        gdprString
    );

    gdprData = userPrivacyUtil.getGdprData();
  }

  private void setupGdprDataWithTcf1(String gdprApplies, String vendorConsents, String tcString) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("IABConsent_SubjectToGDPR", gdprApplies);
    editor.putString("IABConsent_ParsedVendorConsents", vendorConsents);
    editor.putString("IABConsent_ConsentString", tcString);
    editor.apply();
  }

  private void setupGdprDataWithTcf2(String subjectToGdpr, String vendorConsents, String consentString) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("IABTCF_gdprApplies", consentString);
    editor.putString("IABTCF_VendorConsents", vendorConsents);
    editor.putString("IABTCF_TCString", consentString);
    editor.apply();
  }

  private void cleanupTcf1() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.remove("IABConsent_ConsentString");
    editor.remove("IABConsent_SubjectToGDPR");
    editor.remove("IABConsent_ParsedVendorConsents");
    editor.apply();
  }

  private void cleanupTcf2() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.remove("IABTCF_gdprApplies");
    editor.remove("IABTCF_VendorConsents");
    editor.remove("IABTCF_TCString");
    editor.apply();
  }
}
