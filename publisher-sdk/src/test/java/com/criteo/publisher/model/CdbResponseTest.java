package com.criteo.publisher.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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
    CdbResponse cdbResponse = new CdbResponse(this.cdbResponse);
    assertEquals(300, cdbResponse.getTimeToNextCall());
  }

  @Test
  public void testInValidTimeToCallInCdbResponse() throws JSONException {
    this.cdbResponse.put(TIME_TO_NEXT_CALL, "xyz");
    CdbResponse cdbResponse = new CdbResponse(this.cdbResponse);
    assertEquals(0, cdbResponse.getTimeToNextCall());
  }

  @Test
  public void testValidSlotInCdbResponse() throws JSONException {
    CdbResponse cdbResponse = new CdbResponse(this.cdbResponse);
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
  public void new_GivenUserLevelSilent_ContainsTimeToNextCallAndEmptySlot() throws Exception {
    String json = "{\"slots\":[],\"timeToNextCall\":30}";
    CdbResponse cdbResponse = new CdbResponse(new JSONObject(json));

    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(30);
    assertThat(cdbResponse.getSlots()).isEmpty();
  }

  @Test
  public void new_GivenEmptyJson_ContainsNoSlotAndNoTimeToNextCall() throws Exception {
    String json = "{}";
    CdbResponse cdbResponse = new CdbResponse(new JSONObject(json));

    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(0);
    assertThat(cdbResponse.getSlots()).isEmpty();
  }

}
