package com.criteo.publisher.model;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class UserTest {

  @Test
  public void testToJson_AllFieldsProvided() throws Exception {
    User user = User.create(
        "deviceId",
        "fake_mopub_consent",
        "fake_usp_iab",
        "true" /* uspOptout */
    );

    JSONObject jsonObject = user.toJson();

    assertEquals("deviceId", jsonObject.get("deviceId"));
    assertEquals("gaid", jsonObject.get("deviceIdType"));
    assertEquals("android", jsonObject.get("deviceOs"));
    assertEquals("fake_usp_iab", jsonObject.get("uspIab"));
    assertEquals("true", jsonObject.get("uspOptout"));
    assertEquals("fake_mopub_consent", jsonObject.get("mopubConsent"));
  }

  @Test
  public void testToJson_UspValuesNotProvided() throws Exception {
    User user = User.create(
        "deviceId",
        null,
        null,
        null
    );

    JSONObject jsonObject = user.toJson();

    assertFalse(jsonObject.has("uspIab"));
    assertFalse(jsonObject.has("uspOptout"));
    assertFalse(jsonObject.has("mopubConsent"));
  }
}
