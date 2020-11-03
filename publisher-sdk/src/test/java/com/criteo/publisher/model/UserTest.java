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

package com.criteo.publisher.model;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.JsonSerializer;
import com.criteo.publisher.util.JsonSerializerExtensionsKt;
import java.util.HashMap;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;

public class UserTest {

  @Rule
  public final MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private JsonSerializer serializer;

  @Test
  public void testToJson_AllFieldsProvided() throws Exception {
    User user = User.create(
        "deviceId",
        "fake_mopub_consent",
        "fake_usp_iab",
        "true" /* uspOptout */,
        new HashMap<>()
    );

    JSONObject jsonObject = toJson(user);

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
        null,
        new HashMap<>()
    );

    JSONObject jsonObject = toJson(user);

    assertFalse(jsonObject.has("uspIab"));
    assertFalse(jsonObject.has("uspOptout"));
    assertFalse(jsonObject.has("mopubConsent"));
  }

  private JSONObject toJson(User user) throws JSONException {
    return new JSONObject(JsonSerializerExtensionsKt.writeIntoString(serializer, user));
  }
}
