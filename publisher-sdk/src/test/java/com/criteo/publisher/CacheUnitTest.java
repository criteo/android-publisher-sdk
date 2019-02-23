package com.criteo.publisher;

import android.util.Pair;

import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.Slot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CacheUnitTest {
    private SdkCache cache;
    private JSONArray slots;

    @Test
    public void cacheSize() throws JSONException {
        initializeCache();
        assertEquals(slots.length(), cache.getItemCount());
    }

    @Test
    public void ttlTest() throws JSONException {
        initializeCache();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String placement = slots.getJSONObject(0).getString("placementId");
        int width = slots.getJSONObject(0).getInt("width");
        int height = slots.getJSONObject(0).getInt("height");
        Pair<String, String> pair = new Pair<>(placement, width + "x" + height);
        assertNull(cache.getAdUnit(pair));
    }

    @Test
    public void getOneAdUnitTest() throws JSONException {
        initializeCache();
        String placement = slots.getJSONObject(0).getString("placementId");
        int width = slots.getJSONObject(0).getInt("width");
        int height = slots.getJSONObject(0).getInt("height");
        Pair<String, String> pair = new Pair<>(placement, width + "x" + height);
        assertNull(cache.getAdUnit(pair));

    }

    private void initializeCache() throws JSONException {
        String json = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":0,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"},{\"placementId\":\"/140800857/Endeavour_Interstitial_320x480\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":480,\"ttl\":0,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
        JSONObject element = new JSONObject(json);
        slots = element.getJSONArray("slots");
        cache = new SdkCache();
        for (int i = 0; i < slots.length(); i++) {
            Slot slot = new Slot(slots.getJSONObject(i));
            cache.add(slot);
        }
    }
}
