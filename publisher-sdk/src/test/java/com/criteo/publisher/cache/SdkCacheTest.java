package com.criteo.publisher.cache;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.Slot;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SdkCacheTest {
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
        AdSize adSize = new AdSize(slots.getJSONObject(0).getInt("width"), slots.getJSONObject(0).getInt("height"));
        assertNull(cache.getAdUnit(placement, adSize.getFormattedSize()));
    }

    @Test
    public void getOneAdUnitTest() throws JSONException {
        initializeCache();
        String placement = slots.getJSONObject(0).getString("placementId");
        AdSize adSize = new AdSize(slots.getJSONObject(0).getInt("width"), slots.getJSONObject(0).getInt("height"));
        assertNull(cache.getAdUnit(placement, adSize.getFormattedSize()));

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

    @Test
    public void testCacheWithNativeSlot() {
        String cdbStringResponse = "{\n" +
                "    \"slots\": [{\n" +
                "        \"placementId\": \"/140800857/Endeavour_Native\",\n" +
                "        \"cpm\": \"0.04\",\n" +
                "        \"currency\": \"USD\",\n" +
                "        \"width\": 2,\n" +
                "        \"height\": 2,\n" +
                "        \"ttl\": 3600,\n" +
                "        \"native\": {\n" +
                "            \"products\": [{\n" +
                "                \"title\": \"\\\"Stripe Pima Dress\\\" - $99\",\n" +
                "                \"description\": \"We're All About Comfort.\",\n" +
                "                \"price\": \"$99\",\n" +
                "                \"clickUrl\": \"https://cat.sv.us.criteo.com/delivery/ckn.php\",\n" +
                "                \"callToAction\": \"\",\n" +
                "                \"image\": {\n" +
                "                    \"url\": \"https://pix.us.criteo.net/img/img?\",\n" +
                "                    \"height\": 400,\n" +
                "                    \"width\": 400\n" +
                "                }\n" +
                "            }],\n" +
                "            \"advertiser\": {\n" +
                "                \"description\": \"The Company Store\",\n" +
                "                \"domain\": \"thecompanystore.com\",\n" +
                "                \"logo\": {\n" +
                "                    \"url\": \"https://pix.us.criteo.net/img/img\",\n" +
                "                    \"height\": 200,\n" +
                "                    \"width\": 200\n" +
                "                },\n" +
                "                \"logoClickUrl\": \"https://cat.sv.us.criteo.com/delivery/ckn.php\"\n" +
                "            },\n" +
                "            \"privacy\": {\n" +
                "                \"optoutClickUrl\": \"https://privacy.us.criteo.com/adcenter\",\n" +
                "                \"optoutImageUrl\": \"https://static.criteo.net/flash/icon/nai_small.png\",\n" +
                "                \"longLegalText\": \"\"\n" +
                "            },\n" +
                "            \"impressionPixels\": [{\n" +
                "                \"url\": \"https://cat.sv.us.criteo.com/delivery/lgn.php?\"},{\n" +
                "                \"url\": \"https://dog.da.us.criteo.com/delivery/lgn.php?\"\n" +
                "            }]\n" +
                "        }\n" +
                "    }]\n" +
                "}";

        try {
            JSONObject cdbResponse = new JSONObject(cdbStringResponse);
            JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
            Slot slot = new Slot(cdbSlot);
            cache = new SdkCache();
            cache.add(slot);
            assertEquals(1, cache.getItemCount());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }
}
