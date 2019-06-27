package com.criteo.publisher.model;

import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CdbTest {
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
        cdbResponse.put(TIME_TO_NEXT_CALL, 300);
        Cdb cdb = new Cdb(cdbResponse);
        assertEquals(300, cdb.getTimeToNextCall());
    }

    @Test
    public void testInValidTimeToCallInCdbResponse() throws JSONException {
        cdbResponse.put(TIME_TO_NEXT_CALL, "xyz");
        Cdb cdb = new Cdb(cdbResponse);
        assertEquals(0, cdb.getTimeToNextCall());
    }

    @Test
    public void testValidSlotInCdbResponse() throws JSONException {
        Cdb cdb = new Cdb(cdbResponse);
        assertEquals(bid.getString(PLACEMENT_ID), cdb.getSlots().get(0).getPlacementId());
        assertEquals(bid.getString(CPM), cdb.getSlots().get(0).getCpm());
        assertEquals(bid.getString(CURRENCY), cdb.getSlots().get(0).getCurrency());
        assertEquals(bid.getInt(WIDTH), cdb.getSlots().get(0).getWidth());
        assertEquals(bid.getInt(HEIGHT), cdb.getSlots().get(0).getHeight());
        assertEquals(bid.getInt(TTL), cdb.getSlots().get(0).getTtl());
        assertEquals(bid.getString(DISPLAY_URL), cdb.getSlots().get(0).getDisplayUrl());
        assertEquals(0, cdb.getTimeToNextCall());
    }
}
