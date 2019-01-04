package com.criteo.pubsdk;

import android.util.Pair;

import com.criteo.pubsdk.cache.SdkCache;
import com.criteo.pubsdk.model.Slot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CacheUnitTest {
    private SdkCache cache;
    private JsonArray slots;

    @Test
    public void cacheSize() {
        initializeCache();
        assertEquals(slots.size(), cache.getItemCount());
    }

    @Test
    public void ttlTest() {
        initializeCache();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String placement = slots.get(0).getAsJsonObject().get("placementId").getAsString();
        int width = slots.get(0).getAsJsonObject().get("width").getAsInt();
        int height = slots.get(0).getAsJsonObject().get("height").getAsInt();
        Pair<String, String> pair = new Pair<>(placement, width + "x" + height);
        assertNull(cache.getAdUnit(pair));
    }

    @Test
    public void getOneAdUnitTest() {
        initializeCache();
        String placement = slots.get(0).getAsJsonObject().get("placementId").getAsString();
        int width = slots.get(0).getAsJsonObject().get("width").getAsInt();
        int height = slots.get(0).getAsJsonObject().get("height").getAsInt();
        Pair<String, String> pair = new Pair<>(placement, width + "x" + height);
        assertNull(cache.getAdUnit(pair));

    }

    private void initializeCache() {
        String json = "{\n" +
                "    \"slots\": [\n" +
                "        {\n" +
                "            \"placementId\": \"adunitid1\",\n" +
                "            \"cpm\": 1.1200000047683716,\n" +
                "            \"currency\": \"EUR\",\n" +
                "            \"width\": 300,\n" +
                "            \"height\": 250,\n" +
                "            \"ttl\": 0,\n" +
                "            \"creative\": \"<img src='https://demo.criteo.com/publishertag/preprodtest/creative.png' width='300' height='250' />\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"placementId\": \"adunitid2\",\n" +
                "            \"cpm\": 1.1200000047683716,\n" +
                "            \"currency\": \"EUR\",\n" +
                "            \"width\": 300,\n" +
                "            \"height\": 250,\n" +
                "            \"ttl\": 0,\n" +
                "            \"creative\": \"<img src='https://demo.criteo.com/publishertag/preprodtest/creative.png' width='300' height='250' />\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(json);
        slots = element.getAsJsonObject().getAsJsonArray("slots");
        cache = new SdkCache();
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = new Slot(slots.get(i).getAsJsonObject());
            cache.add(slot);
        }
    }
}
