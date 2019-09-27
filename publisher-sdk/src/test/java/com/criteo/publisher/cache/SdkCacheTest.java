package com.criteo.publisher.cache;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Slot;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
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
    public void getAdUnitFromCacheTest() throws JSONException {
        initializeCache();

        for(int i=slots.length() -1; i >=0 ;i--) {
            String placement = slots.getJSONObject(i).getString("placementId");
            AdSize adSize = new AdSize(slots.getJSONObject(i).getInt("width"), slots.getJSONObject(i).getInt("height"));
            CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
            Slot cachedSlot = cache.getAdUnit(testAdUnit);
            assertEquals(placement, cachedSlot.getPlacementId());
            assertEquals(slots.getJSONObject(i).getString("currency"), cachedSlot.getCurrency());
            assertEquals(slots.getJSONObject(i).getString("cpm"), cachedSlot.getCpm());
            assertEquals(slots.getJSONObject(i).getString("displayUrl"), cachedSlot.getDisplayUrl());
            assertEquals(slots.getJSONObject(i).getInt("width"), cachedSlot.getWidth());
            assertEquals(slots.getJSONObject(i).getInt("height"), cachedSlot.getHeight());
        }
    }

    @Test
    public void getNotCachedAdUnitFromCacheTest() throws JSONException {
        initializeCache();

        String placement = "this/isnt/in/the/cache";
        AdSize adSize = new AdSize(320, 50);
        CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
        Slot cachedSlot = cache.getAdUnit(testAdUnit);
        assertNull(cachedSlot);
    }

    @Test
    public void peekAdUnitFromCacheTest() throws JSONException {
        initializeCache();

        for(int i=slots.length() -1; i >=0 ;i--) {
            String placement = slots.getJSONObject(i).getString("placementId");
            AdSize adSize = new AdSize(slots.getJSONObject(i).getInt("width"), slots.getJSONObject(i).getInt("height"));
            CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
            Slot cachedSlot = cache.peekAdUnit(testAdUnit);
            assertEquals(placement, cachedSlot.getPlacementId());
            assertEquals(slots.getJSONObject(i).getString("currency"), cachedSlot.getCurrency());
            assertEquals(slots.getJSONObject(i).getString("cpm"), cachedSlot.getCpm());
            assertEquals(slots.getJSONObject(i).getString("displayUrl"), cachedSlot.getDisplayUrl());
            assertEquals(slots.getJSONObject(i).getInt("width"), cachedSlot.getWidth());
            assertEquals(slots.getJSONObject(i).getInt("height"), cachedSlot.getHeight());
        }
    }

    @Test
    public void peekNotCachedAdUnitFromCacheTest() throws JSONException {
        initializeCache();

        String placement = "this/isnt/in/the/cache";
        AdSize adSize = new AdSize(320, 50);
        CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
        Slot cachedSlot = cache.peekAdUnit(testAdUnit);
        assertNull(cachedSlot);
    }

    @Test
    public void removeAdUnitFromCacheTest() throws JSONException {
        initializeCache();

        for(int i=slots.length() -1; i >=0 ;i--) {
            String placement = slots.getJSONObject(i).getString("placementId");
            AdSize adSize = new AdSize(slots.getJSONObject(i).getInt("width"), slots.getJSONObject(i).getInt("height"));
            CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
            Slot cachedSlot = cache.peekAdUnit(testAdUnit);
            assertEquals(placement, cachedSlot.getPlacementId());
            assertEquals(slots.getJSONObject(i).getString("currency"), cachedSlot.getCurrency());
            assertEquals(slots.getJSONObject(i).getString("cpm"), cachedSlot.getCpm());
            assertEquals(slots.getJSONObject(i).getString("displayUrl"), cachedSlot.getDisplayUrl());
            assertEquals(slots.getJSONObject(i).getInt("width"), cachedSlot.getWidth());
            assertEquals(slots.getJSONObject(i).getInt("height"), cachedSlot.getHeight());

            cache.remove(testAdUnit);
            assertNull(cache.peekAdUnit(testAdUnit));
            assertNull(cache.getAdUnit(testAdUnit));
            assertEquals(i, cache.getItemCount());
        }
    }

    @Test
    public void removeNotCachedAdUnitFromCacheTest() throws JSONException {
        initializeCache();

        String placement = "this/isnt/in/the/cache";
        AdSize adSize = new AdSize(320, 50);
        CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
        int cacheSize = cache.getItemCount();
        cache.remove(testAdUnit);
        assertEquals(cacheSize, cache.getItemCount());
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
    public void testAdditionOfInvalidSlot() {
        // One is missing a displayUrl and the other has a negative cpm
        // Neither bid should be added to the cache
        String json = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"0.00\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":0,\"displayUrl\":\"\"},{\"placementId\":\"/140800857/Endeavour_Interstitial_320x480\",\"cpm\":\"-1.00\",\"currency\":\"EUR\",\"width\":320,\"height\":480,\"ttl\":0,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
        try {
            JSONObject element = new JSONObject(json);
            slots = element.getJSONArray("slots");
            cache = new SdkCache();
            for (int i = 0; i < slots.length(); i++) {
                Slot slot = new Slot(slots.getJSONObject(i));
                cache.add(slot);
            }
            Assert.assertEquals(0, cache.getItemCount());
        } catch (Exception ex) {
            Assert.fail("json parsing failed " + ex.getLocalizedMessage());
        }
    }

    @Test
    public void testAdditionOfInvalidNativeSlot() {
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
                "                \"optoutClickUrl\": \"\",\n" +
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
            assertEquals(0, cache.getItemCount());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
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
