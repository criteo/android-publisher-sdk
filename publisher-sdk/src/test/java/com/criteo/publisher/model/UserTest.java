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
    user.setUspIab("1YNNN");

    JSONObject jsonObject = user.toJson();

    assertEquals("deviceId", jsonObject.get("deviceId"));
    assertEquals("gaid", jsonObject.get("deviceIdType"));
    assertEquals("deviceModel", jsonObject.get("deviceModel"));
    assertEquals("android", jsonObject.get("deviceOs"));
    assertEquals(BuildConfig.VERSION_NAME, jsonObject.get("sdkver"));
    assertEquals(0, jsonObject.get("lmt"));
    assertEquals("1YNNN", jsonObject.get("uspIab"));
  }

  @Test
  public void testToJson_UspValuesNotProvided() throws Exception {
    JSONObject jsonObject = user.toJson();

    assertFalse(jsonObject.has("uspIab"));
    assertFalse(jsonObject.has("uspOptout"));
  }
}

