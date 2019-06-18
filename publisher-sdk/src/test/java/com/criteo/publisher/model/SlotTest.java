package com.criteo.publisher.model;

import com.criteo.publisher.model.Slot;

import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SlotTest {
    private static final int DEFAULT_TTL = 15 * 60 * 1000;
    private static final String CPM = "cpm";
    private static final String DISPLAY_URL = "displayUrl";
    private static final String PLACEMENT_ID = "placementId";

    private Slot slot;
    private JSONObject response;

    @Before
    public void prepare() throws JSONException {
        response = new JSONObject();
        response.put(PLACEMENT_ID, "/140800857/Endeavour_320x50");
    }

    @Test
    public void noBidTest() throws JSONException {
        response.put("cpm", "0");
        response.put("ttl", 0);
        Slot result = new Slot(response);
        assertEquals("0", result.getCpm());
        assertEquals(0, result.getTtl());
    }

    @Test
    public void silentModeTest() throws JSONException {
        int ttlval = 50*60;
        response.put("cpm", "0");
        response.put("ttl", ttlval);
        Slot result = new Slot(response);
        assertEquals("0", result.getCpm());
        assertEquals(ttlval, result.getTtl());
    }

    @Test
    public void bidTest() throws JSONException {
        String cpmval = "1.5";
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
        response.put("cpm", cpmval);
        response.put("ttl", ttlval);
        Slot result = new Slot(response);
        assertEquals(cpmval, result.getCpm());
        assertEquals(ttlval, result.getTtl());
    }

    @Test
    public void testSlot() throws JSONException {
        response.put(CPM, "10.0");
        response.put(DISPLAY_URL, "https://www.criteo.com/");
        slot = new Slot(response);
        Assert.assertTrue(slot.isValid());
    }

    @Test
    public void testSlotWithNullDisplayUrlNullCmp() {
        slot = new Slot(response);
        Assert.assertFalse(slot.isValid());
    }

    @Test
    public void testSlotWithNullDisplayUrl() throws JSONException {
        response.put(CPM, "10.0");
        slot = new Slot(response);
        Assert.assertFalse(slot.isValid());
    }

    @Test
    public void testSlotWithEmptyDisplayUrl() throws JSONException {
        response.put(CPM, "10.0");
        response.put(DISPLAY_URL, "");
        slot = new Slot(response);
        Assert.assertFalse(slot.isValid());
    }

    @Test
    public void testSlotWithNullCmp() throws JSONException {
        response.put(DISPLAY_URL, "https://www.criteo.com/");
        slot = new Slot(response);
        Assert.assertTrue(slot.isValid());
    }

    @Test
    public void testSlotWithInvalidCmp() throws JSONException {
        response.put(DISPLAY_URL, "https://www.criteo.com/");
        response.put(CPM, "abc");
        slot = new Slot(response);
        Assert.assertFalse(slot.isValid());
    }

    @Test
    public void testSlotWithNegativeValueCmp() throws JSONException {
        response.put(DISPLAY_URL, "https://www.criteo.com/");
        response.put(CPM, "-10.0");
        slot = new Slot(response);
        Assert.assertFalse(slot.isValid());
    }

    @Test
    public void testSlotWithEmptyValueCmp() throws JSONException {
        response.put(DISPLAY_URL, "https://www.criteo.com/");
        response.put(CPM, "");
        slot = new Slot(response);
        Assert.assertFalse(slot.isValid());
    }


}
