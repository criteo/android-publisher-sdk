package com.criteo.publisher.cache;

import com.criteo.publisher.model.Slot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SlotTest {
    private static final int DEFAULT_TTL = 15 * 60 * 1000;

    @Test
    public void noBidTest() throws JSONException {
        JSONObject response = new JSONObject();
        response.put("cpm", "0");
        response.put("ttl", 0);
        Slot result = new Slot(response);
        assertEquals("0", result.getCpm());
        assertEquals(0, result.getTtl());
    }

    @Test
    public void silentModeTest() throws JSONException {
        int ttlval = 50*60;
        JSONObject response = new JSONObject();
        response.put("cpm", "0");
        response.put("ttl", ttlval);
        Slot result = new Slot(response);
        assertEquals("0", result.getCpm());
        assertEquals(ttlval, result.getTtl());
    }

    @Test
    public void bidTest() throws JSONException {
        String cpmval = "1.5";
        JSONObject response = new JSONObject();
        response.put("cpm", cpmval);
        response.put("ttl", 0);
        Slot result = new Slot(response);
        assertEquals(cpmval, result.getCpm());
        assertEquals(DEFAULT_TTL, result.getTtl());
    }

    @Test
    public void bidCachingTest() throws JSONException {
        String cpmval = "1.5";
        int ttlval = 50*60;
        JSONObject response = new JSONObject();
        response.put("cpm", cpmval);
        response.put("ttl", ttlval);
        Slot result = new Slot(response);
        assertEquals(cpmval, result.getCpm());
        assertEquals(ttlval, result.getTtl());
    }




}
