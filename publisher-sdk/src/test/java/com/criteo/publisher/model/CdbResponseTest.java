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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class CdbResponseTest {

  private static final String SLOTS = "slots";
  private static final String TIME_TO_NEXT_CALL = "timeToNextCall";
  private static final String PLACEMENT_ID = "placementId";
  private static final String CPM = "cpm";
  private static final String CURRENCY = "currency";
  private static final String WIDTH = "width";
  private static final String HEIGHT = "height";
  private static final String TTL = "ttl";
  private static final String DISPLAY_URL = "displayUrl";
  private JSONObject bid;
  private JSONObject cdbResponse;

  @Before
  public void prepare() throws JSONException {
    cdbResponse = new JSONObject();
    JSONArray slot = new JSONArray();
    bid = new JSONObject();
    bid.put("placementId", "/140800857/Endeavour_320x50");
    bid.put("cpm", "300.00");
    bid.put("currency", "TRY");
    bid.put("width", 320);
    bid.put("height", 50);
    bid.put("ttl", 3000);
    bid.put("displayUrl", "test");
    slot.put(bid);
    cdbResponse.put(SLOTS, slot);
  }

  @Test
  public void testValidTimeToCallInCdbResponse() throws JSONException {
    this.cdbResponse.put(TIME_TO_NEXT_CALL, 300);
    CdbResponse cdbResponse = CdbResponse.fromJson(this.cdbResponse);
    assertEquals(300, cdbResponse.getTimeToNextCall());
  }

  @Test
  public void testInValidTimeToCallInCdbResponse() throws JSONException {
    this.cdbResponse.put(TIME_TO_NEXT_CALL, "xyz");
    CdbResponse cdbResponse = CdbResponse.fromJson(this.cdbResponse);
    assertEquals(0, cdbResponse.getTimeToNextCall());
  }

  @Test
  public void testValidSlotInCdbResponse() throws JSONException {
    CdbResponse cdbResponse = CdbResponse.fromJson(this.cdbResponse);
    assertEquals(bid.getString(PLACEMENT_ID), cdbResponse.getSlots().get(0).getPlacementId());
    assertEquals(bid.getString(CPM), cdbResponse.getSlots().get(0).getCpm());
    assertEquals(bid.getString(CURRENCY), cdbResponse.getSlots().get(0).getCurrency());
    assertEquals(bid.getInt(WIDTH), cdbResponse.getSlots().get(0).getWidth());
    assertEquals(bid.getInt(HEIGHT), cdbResponse.getSlots().get(0).getHeight());
    assertEquals(bid.getInt(TTL), cdbResponse.getSlots().get(0).getTtl());
    assertEquals(bid.getString(DISPLAY_URL), cdbResponse.getSlots().get(0).getDisplayUrl());
    assertEquals(0, cdbResponse.getTimeToNextCall());
  }

  @Test
  public void fromJson_GivenUserLevelSilent_ContainsTimeToNextCallAndEmptySlot() throws Exception {
    String json = "{\"slots\":[],\"timeToNextCall\":30}";
    CdbResponse cdbResponse = CdbResponse.fromJson(new JSONObject(json));

    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(30);
    assertThat(cdbResponse.getSlots()).isEmpty();
  }

  @Test
  public void fromJson_GivenEmptyJson_ContainsNoSlotAndNoTimeToNextCall() throws Exception {
    String json = "{}";
    CdbResponse cdbResponse = CdbResponse.fromJson(new JSONObject(json));

    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(0);
    assertThat(cdbResponse.getSlots()).isEmpty();
  }

  @Test
  public void getSlotByImpressionId_GivenEmptySlots_ReturnNull() throws Exception {
    CdbResponse cdbResponse = new CdbResponse(emptyList(), 0);

    assertThat(cdbResponse.getSlotByImpressionId("id")).isNull();
  }

  @Test
  public void getSlotByImpressionId_GivenSlotsThatDoesNotMatchGivenId_ReturnNull() throws Exception {
    Slot slot1 = mock(Slot.class);
    Slot slot2 = mock(Slot.class);
    Slot slot3 = mock(Slot.class);

    when(slot1.getImpressionId()).thenReturn("impId1");
    when(slot2.getImpressionId()).thenReturn(null);
    when(slot3.getImpressionId()).thenReturn("impId3");

    CdbResponse cdbResponse = new CdbResponse(asList(slot1, slot2, slot3), 0);

    assertThat(cdbResponse.getSlotByImpressionId("id")).isNull();
  }

  @Test
  public void getSlotByImpressionId_GivenSlotsMatchingGivenId_ReturnSlot() throws Exception {
    Slot slot1 = mock(Slot.class);
    Slot slot2 = mock(Slot.class);
    Slot slot3 = mock(Slot.class);

    when(slot1.getImpressionId()).thenReturn("impId1");
    when(slot2.getImpressionId()).thenReturn(null);
    when(slot3.getImpressionId()).thenReturn("id");

    CdbResponse cdbResponse = new CdbResponse(asList(slot1, slot2, slot3), 0);

    assertThat(cdbResponse.getSlotByImpressionId("id")).isEqualTo(slot3);
  }

}
