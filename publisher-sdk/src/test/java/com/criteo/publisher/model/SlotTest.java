package com.criteo.publisher.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SlotTest {

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
    int ttlval = 50 * 60;
    response.put("cpm", "0");
    response.put("ttl", ttlval);
    Slot result = new Slot(response);
    assertEquals("0", result.getCpm());
    assertEquals(ttlval, result.getTtl());
  }

  @Test
  public void bidCachingTest() throws JSONException {
    String cpmval = "1.5";
    int ttlval = 50 * 60;
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
      Assert.assertEquals("\"Stripe Pima Dress\" - $99",
          slot.getNativeAssets().getNativeProducts().get(0).getTitle());
      Assert.assertEquals("We're All About Comfort.",
          slot.getNativeAssets().getNativeProducts().get(0).getDescription());
      Assert.assertEquals("$99", slot.getNativeAssets().getNativeProducts().get(0).getPrice());
      Assert.assertEquals("The Company Store", slot.getNativeAssets().getAdvertiserDescription());
      Assert.assertEquals("https://pix.us.criteo.net/img/img",
          slot.getNativeAssets().getAdvertiserLogoUrl());
      Assert.assertEquals("https://privacy.us.criteo.com/adcenter",
          slot.getNativeAssets().getPrivacyOptOutClickUrl());
      Assert.assertEquals(2, slot.getNativeAssets().getImpressionPixels().size());
      Assert.assertEquals("https://cat.sv.us.criteo.com/delivery/lgn.php?",
          slot.getNativeAssets().getImpressionPixels().get(0));
      Assert.assertEquals("https://dog.da.us.criteo.com/delivery/lgn.php?",
          slot.getNativeAssets().getImpressionPixels().get(1));
      Assert.assertTrue(slot.isNative());
      Assert.assertTrue(slot.isValid());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.assertEquals("https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js",
          slot.getDisplayUrl());
      Assert.assertFalse(slot.isNative());
      Assert.assertNull(slot.getNativeAssets());
      Assert.assertTrue(slot.isValid());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.assertEquals("The Company Store", slot.getNativeAssets().getAdvertiserDescription());
      Assert.assertFalse(slot.isValid());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.assertEquals("The Company Store", slot.getNativeAssets().getAdvertiserDescription());
      Assert.assertFalse(slot.isValid());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.assertEquals("The Company Store", slot.getNativeAssets().getAdvertiserDescription());
      Assert.assertFalse(slot.isValid());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
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
      Assert.assertEquals("The Company Store", slot.getNativeAssets().getAdvertiserDescription());
      Assert.assertFalse(slot.isValid());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
    }
  }

  @Test
  public void testValidityWhenSlotIsNative() {
    Slot slot = new Slot(getNativeJSONSlot());
    Assert.assertTrue(slot.isValid());
    Assert.assertTrue(slot.isNative());
    // if a slot claims it is native and valid then all the following conditions have to be met
    // it contains a cpm
    Assert.assertNotNull(slot.getCpm());
    Assert.assertTrue(slot.getCpm().length() > 0);
    Assert.assertTrue(slot.getCpmAsNumber() >= 0.0d);
    Assert.assertNotNull(slot.getNativeAssets());
    // it contains at least one product
    Assert.assertNotNull(slot.getNativeAssets().getNativeProducts());
    Assert.assertTrue(slot.getNativeAssets().getNativeProducts().size() > 0);
    // it contains at least one impression pixel
    Assert.assertNotNull(slot.getNativeAssets().getImpressionPixels());
    Assert.assertTrue(slot.getNativeAssets().getImpressionPixels().size() > 0);
    // it contains the opt out click url and an opt out image
    // checking if the string is a valid url or not is beyond the scope of the SDK for now
    Assert.assertNotNull(slot.getNativeAssets().getPrivacyOptOutClickUrl());
    Assert.assertTrue(slot.getNativeAssets().getPrivacyOptOutClickUrl().length() > 0);
    Assert.assertFalse("".equals(slot.getNativeAssets().getPrivacyOptOutClickUrl()));
    Assert.assertNotNull(slot.getNativeAssets().getPrivacyOptOutImageUrl());
    Assert.assertTrue(slot.getNativeAssets().getPrivacyOptOutImageUrl().length() > 0);
    Assert.assertFalse("".equals(slot.getNativeAssets().getPrivacyOptOutImageUrl()));
  }

  @Test
  public void testValidity() {
    Slot slot = new Slot(getJSONSlot());
    Assert.assertTrue(slot.isValid());
    Assert.assertFalse(slot.isNative());
    // if a slot claims it is NOT native and valid then all the following conditions have to be met
    // it contains a cpm
    Assert.assertNotNull(slot.getCpm());
    Assert.assertTrue(slot.getCpm().length() > 0);
    Assert.assertTrue(slot.getCpmAsNumber() >= 0.0d);
    Assert.assertNull(slot.getNativeAssets());
    // it contains a displayUrl
    Assert.assertNotNull(slot.getDisplayUrl());
    Assert.assertTrue(slot.getDisplayUrl().length() > 0);
    Assert.assertFalse("".equals(slot.getDisplayUrl()));
  }

  @Test
  public void isValid_GivenMissingDisplayUrlOrNegativeCpm_ReturnFalse() throws Exception {
    // One is missing a displayUrl and the other has a negative cpm
    // Neither bid should be added to the cache
    String json = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"0.00\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":0,\"displayUrl\":\"\"},{\"placementId\":\"/140800857/Endeavour_Interstitial_320x480\",\"cpm\":\"-1.00\",\"currency\":\"EUR\",\"width\":320,\"height\":480,\"ttl\":0,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
    List<Slot> slots = new CdbResponse(new JSONObject(json)).getSlots();

    for (Slot slot : slots) {
      assertThat(slot.isValid()).isFalse();
    }
  }

  @Test
  public void isValid_GivenInvalidNativePayload_ReturnFalse() throws Exception {
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
        "                \"optoutImageUrl\": \"https://static.criteo.net/flash/icon/nai_small.png\",\n"
        +
        "                \"longLegalText\": \"\"\n" +
        "            },\n" +
        "            \"impressionPixels\": [{\n" +
        "                \"url\": \"https://cat.sv.us.criteo.com/delivery/lgn.php?\"},{\n" +
        "                \"url\": \"https://dog.da.us.criteo.com/delivery/lgn.php?\"\n" +
        "            }]\n" +
        "        }\n" +
        "    }]\n" +
        "}";

    List<Slot> slots = new CdbResponse(new JSONObject(cdbStringResponse)).getSlots();

    for (Slot slot : slots) {
      assertThat(slot.isValid()).isFalse();
    }
  }

  @Test
  public void isValid_GivenInvalidDisplayUrlForBannerOrInterstitial_ReturnFalse() throws Exception {
    String json = "{\n"
        + "  \"placementId\": \"myAdUnit\",\n"
        + "  \"cpm\": \"20.00\",\n"
        + "  \"currency\": \"USD\",\n"
        + "  \"width\": 100,\n"
        + "  \"height\": 100,\n"
        + "  \"ttl\": 60,\n"
        + "  \"displayUrl\": \"notAValidUrl\"\n"
        + "}";
    Slot slot = new Slot(new JSONObject(json));

    assertThat(slot.isValid()).isFalse();
  }

  @Test
  public void getTtl_GivenImmediateBid_ShouldNotOverrideTtl() throws Exception {
    // Immediate bid means CPM > 0 and TTL = 0
    // Business logic is managed by the BidManager. This is only expected to decode CDB payload.

    String json = "{\n"
        + "  \"placementId\": \"myAdUnit\",\n"
        + "  \"cpm\": \"20.00\",\n"
        + "  \"currency\": \"USD\",\n"
        + "  \"width\": 100,\n"
        + "  \"height\": 100,\n"
        + "  \"ttl\": 0,\n"
        + "  \"displayUrl\": \"http://criteo.com\"\n"
        + "}";
    Slot slot = new Slot(new JSONObject(json));

    assertThat(slot.getTtl()).isZero();
  }

  @Test
  public void getTtl_GivenNoTtl_ShouldNotOverrideTtl() throws Exception {
    String json = "{\n"
        + "  \"placementId\": \"myAdUnit\",\n"
        + "  \"cpm\": \"20.00\",\n"
        + "  \"currency\": \"USD\",\n"
        + "  \"width\": 100,\n"
        + "  \"height\": 100,\n"
        + "  \"displayUrl\": \"http://criteo.com\"\n"
        + "}";
    Slot slot = new Slot(new JSONObject(json));

    assertThat(slot.getTtl()).isZero();
  }

  private JSONObject getNativeJSONSlot() {
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
      return cdbSlot;
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
    }
    return null;
  }

  private JSONObject getJSONSlot() {
    try {
      String cdbStringResponse = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":555,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
      JSONObject cdbResponse = new JSONObject(cdbStringResponse);
      JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
      return cdbSlot;
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
    }
    return null;
  }
}
