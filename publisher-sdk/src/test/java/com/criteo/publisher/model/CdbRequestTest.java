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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.criteo.publisher.privacy.gdpr.GdprData;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CdbRequestTest {

  @Mock
  private Publisher publisher;

  @Mock
  private User user;

  private String sdkVersion;

  private int profileId;

  @Mock
  private GdprData gdprData;

  @Mock
  private JSONObject gdprConsent;

  private List<CdbRequestSlot> slots;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    sdkVersion = "1.2.3";
    profileId = 42;
    slots = new ArrayList<>();
  }

  @Test
  public void toJson_GivenAllInformation_MapThemToJson() throws Exception {
    JSONObject publisherJson = mock(JSONObject.class);
    when(publisher.toJson()).thenReturn(publisherJson);

    JSONObject userJson = mock(JSONObject.class);
    when(user.toJson()).thenReturn(userJson);

    when(gdprData.toJSONObject()).thenReturn(gdprConsent);

    CdbRequestSlot slot = mock(CdbRequestSlot.class);
    JSONObject slotObject = mock(JSONObject.class);
    when(slot.toJson()).thenReturn(slotObject);

    slots.add(slot);

    CdbRequest request = createRequest();
    JSONObject json = request.toJson();

    assertThat(json.get("user")).isEqualTo(userJson);
    assertThat(json.get("publisher")).isEqualTo(publisherJson);
    assertThat(json.get("sdkVersion")).isEqualTo("1.2.3");
    assertThat(json.get("profileId")).isEqualTo(42);
    assertThat(json.get("gdprConsent")).isEqualTo(gdprConsent);
    assertThat(json.getJSONArray("slots").length()).isEqualTo(1);
    assertThat(json.getJSONArray("slots").get(0)).isEqualTo(slotObject);
  }

  @Test
  public void toJson_GivenNoGdpr_DoesNotMapIt() throws Exception {
    gdprData = null;

    CdbRequest request = createRequest();
    JSONObject json = request.toJson();

    assertThat(json.has("gdprConsent")).isFalse();
  }

  private CdbRequest createRequest() {
    return new CdbRequest(publisher, user, sdkVersion, profileId, gdprData, slots);
  }

}