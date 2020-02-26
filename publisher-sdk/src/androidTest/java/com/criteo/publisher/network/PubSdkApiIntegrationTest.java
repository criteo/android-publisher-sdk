package com.criteo.publisher.network;

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

  @Before
  public void setup() {
    context = InstrumentationRegistry.getContext().getApplicationContext();
    appId = context.getApplicationContext().getPackageName();
    senderId = 2379;
    limitedAdTracking = 0;
    gaid = "021a86de-ef82-4f69-867b-61ca66688c9c";
    eventType = "Launch";
    api = mockedDependenciesRule.getDependencyProvider().providePubSdkApi();


    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString("IABTCF_gdprApplies", "1");
    editor.putString("IABTCF_VendorConsents", "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001");
    editor.putString("IABTCF_TCString", "ssds");
    editor.putString("IABTCF_VendorConsents", "1");
    editor.apply();

    UserPrivacyUtil userPrivacyUtil = mockedDependenciesRule.getDependencyProvider().provideUserPrivacyUtil(context);
    gdprData = userPrivacyUtil.getGdprData();
  }


  @Test
  public void testPostAppEventWithNullGaid() {
    JSONObject object = api
        .postAppEvent(senderId, appId, gaid, eventType, limitedAdTracking, "");
    assertNotNull(object);
  }

  @Test
  public void testPostAppEventWithGaid() {
    gaid = null;
    JSONObject object = api
        .postAppEvent(senderId, appId, gaid, eventType, limitedAdTracking, "");
    assertNotNull(object);
  }
}