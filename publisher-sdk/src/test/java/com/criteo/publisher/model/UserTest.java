package com.criteo.publisher.model;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import com.criteo.publisher.BuildConfig;
import com.criteo.publisher.Util.DeviceUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UserTest {
  @Mock
  private DeviceUtil deviceUtil;

  private User user;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(deviceUtil.getDeviceModel()).thenReturn("deviceModel");
    user = new User(deviceUtil);
    user.setDeviceId("deviceId");
  }

  @Test
  public void testToJson_AllFieldsProvided() throws Exception {
    user.setUspIab("fake_usp_iab");
    user.setUspOptout("true");
    user.setMopubConsent("fake_mopub_consent");
    JSONObject jsonObject = user.toJson();

    assertEquals("deviceId", jsonObject.get("deviceId"));
    assertEquals("gaid", jsonObject.get("deviceIdType"));
    assertEquals("deviceModel", jsonObject.get("deviceModel"));
    assertEquals("android", jsonObject.get("deviceOs"));
    assertEquals(BuildConfig.VERSION_NAME, jsonObject.get("sdkver"));
    assertEquals(0, jsonObject.get("lmt"));
    assertEquals("fake_usp_iab", jsonObject.get("uspIab"));
    assertEquals("true", jsonObject.get("uspOptout"));
    assertEquals("fake_mopub_consent", jsonObject.get("mopubConsent"));
  }

  @Test
  public void testToJson_UspValuesNotProvided() throws Exception {
    JSONObject jsonObject = user.toJson();

    assertFalse(jsonObject.has("uspIab"));
    assertFalse(jsonObject.has("uspOptout"));
    assertFalse(jsonObject.has("mopubConsent"));
  }

  @Test
  public void testToJson_UspValuesEmpty() throws Exception {
    JSONObject jsonObject = user.toJson();
    user.setUspOptout("");
    user.setMopubConsent("");
    user.setUspIab("");

    assertFalse(jsonObject.has("uspIab"));
    assertFalse(jsonObject.has("uspOptout"));
    assertFalse(jsonObject.has("mopubConsent"));
  }
}

