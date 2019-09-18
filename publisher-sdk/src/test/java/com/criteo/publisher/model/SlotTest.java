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

    @Test
    public void testJsonParsingWithNative() {
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
            Assert.assertNotNull(slot.getNativeAssets());
            Assert.assertEquals("\"Stripe Pima Dress\" - $99", slot.getNativeAssets().nativeProducts.get(0).title);
            Assert.assertEquals("We're All About Comfort.", slot.getNativeAssets().nativeProducts.get(0).description);
            Assert.assertEquals("$99", slot.getNativeAssets().nativeProducts.get(0).price);
            Assert.assertEquals("The Company Store", slot.getNativeAssets().advertiserDescription);
            Assert.assertEquals("https://pix.us.criteo.net/img/img", slot.getNativeAssets().advertiserLogoUrl);
            Assert.assertEquals("https://privacy.us.criteo.com/adcenter", slot.getNativeAssets().privacyOptOutClickUrl);
            Assert.assertEquals(2, slot.getNativeAssets().impressionPixels.size());
            Assert.assertEquals("https://cat.sv.us.criteo.com/delivery/lgn.php?", slot.getNativeAssets().impressionPixels.get(0));
            Assert.assertEquals("https://dog.da.us.criteo.com/delivery/lgn.php?", slot.getNativeAssets().impressionPixels.get(1));
            Assert.assertTrue(slot.isNative());
            Assert.assertTrue(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testParsingWithoutNative() {
        try {
            String cdbStringResponse = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":555,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
            JSONObject cdbResponse = new JSONObject(cdbStringResponse);
            JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
            Slot slot = new Slot(cdbSlot);
            Assert.assertEquals("/140800857/Endeavour_320x50", slot.getPlacementId());
            Assert.assertEquals("1.12", slot.getCpm());
            Assert.assertEquals("EUR", slot.getCurrency());
            Assert.assertEquals(320, slot.getWidth());
            Assert.assertEquals(50, slot.getHeight());
            Assert.assertEquals(555, slot.getTtl());
            Assert.assertEquals("https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js", slot.getDisplayUrl());
            Assert.assertFalse(slot.isNative());
            Assert.assertNull(slot.getNativeAssets());
            Assert.assertTrue(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testEquality() {

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
            Slot expectedSlot = new Slot(cdbSlot);
            assertEquals(expectedSlot, slot);
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testJsonParsingForNativeWithoutImpressionPixels() {
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
                "            \"impressionPixels\": []\n" +
                "        }\n" +
                "    }]\n" +
                "}";

        try {
            JSONObject cdbResponse = new JSONObject(cdbStringResponse);
            JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
            Slot slot = new Slot(cdbSlot);
            Assert.assertTrue(slot.isNative());
            Assert.assertNotNull(slot.getNativeAssets());
            Assert.assertEquals(3600, slot.getTtl());
            Assert.assertEquals("/140800857/Endeavour_Native", slot.getPlacementId());
            Assert.assertFalse(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testJsonParsingForNativeWithZeroProducts() {
        String cdbStringResponse = "{\n" +
                "    \"slots\": [{\n" +
                "        \"placementId\": \"/140800857/Endeavour_Native\",\n" +
                "        \"cpm\": \"0.04\",\n" +
                "        \"currency\": \"USD\",\n" +
                "        \"width\": 2,\n" +
                "        \"height\": 2,\n" +
                "        \"ttl\": 3600,\n" +
                "        \"native\": {\n" +
                "            \"products\": [],\n" +
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
            Assert.assertTrue(slot.isNative());
            Assert.assertNotNull(slot.getNativeAssets());
            Assert.assertEquals(3600, slot.getTtl());
            Assert.assertEquals("/140800857/Endeavour_Native", slot.getPlacementId());
            Assert.assertEquals("The Company Store", slot.getNativeAssets().advertiserDescription);
            Assert.assertFalse(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testJsonParsingForNativeWithoutProducts() {
        String cdbStringResponse = "{\n" +
                "    \"slots\": [{\n" +
                "        \"placementId\": \"/140800857/Endeavour_Native\",\n" +
                "        \"cpm\": \"0.04\",\n" +
                "        \"currency\": \"USD\",\n" +
                "        \"width\": 2,\n" +
                "        \"height\": 2,\n" +
                "        \"ttl\": 3600,\n" +
                "        \"native\": {\n" +
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
            Assert.assertTrue(slot.isNative());
            Assert.assertNotNull(slot.getNativeAssets());
            Assert.assertEquals(3600, slot.getTtl());
            Assert.assertEquals("/140800857/Endeavour_Native", slot.getPlacementId());
            Assert.assertEquals("The Company Store", slot.getNativeAssets().advertiserDescription);
            Assert.assertFalse(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testJsonParsingForNativeWithoutPrivacyLogo() {
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
            Assert.assertTrue(slot.isNative());
            Assert.assertNotNull(slot.getNativeAssets());
            Assert.assertEquals(3600, slot.getTtl());
            Assert.assertEquals("/140800857/Endeavour_Native", slot.getPlacementId());
            Assert.assertEquals("The Company Store", slot.getNativeAssets().advertiserDescription);
            Assert.assertFalse(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testJsonParsingForNativeWithoutImpressionPixelsAndPrivacyLogo() {
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
                "                \"longLegalText\": \"\"\n" +
                "            }" +
                "        }\n" +
                "    }]\n" +
                "}";

        try {
            JSONObject cdbResponse = new JSONObject(cdbStringResponse);
            JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
            Slot slot = new Slot(cdbSlot);
            Assert.assertTrue(slot.isNative());
            Assert.assertNotNull(slot.getNativeAssets());
            Assert.assertEquals(3600, slot.getTtl());
            Assert.assertEquals("/140800857/Endeavour_Native", slot.getPlacementId());
            Assert.assertEquals("The Company Store", slot.getNativeAssets().advertiserDescription);
            Assert.assertFalse(slot.isValid());
        } catch (Exception ex) {
            Assert.fail("Json exception in test data : "+ ex.getLocalizedMessage());
        }
    }
}
