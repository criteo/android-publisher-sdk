/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.criteo.publisher.Clock;
import com.criteo.publisher.model.nativeads.NativeAssets;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


public class SlotTest {

  private static final String CPM = "cpm";
  private static final String DISPLAY_URL = "displayUrl";
  private static final String PLACEMENT_ID = "placementId";
  private static final String TTL = "ttl";

  private Slot slot;
  private JSONObject response;

  @Before
  public void prepare() throws JSONException {
    response = new JSONObject();
    response.put(PLACEMENT_ID, "/140800857/Endeavour_320x50");
  }

  @Test
  public void givenEmptyPayload_UseFallback() {
    Slot slot = new Slot(new JSONObject());

    assertThat(slot.getImpressionId()).isNull();
    assertThat(slot.getPlacementId()).isNull();
    assertThat(slot.getCpm()).isEqualTo("0.0");
    assertThat(slot.getCpmAsNumber()).isZero();
    assertThat(slot.getCurrency()).isNull();
    assertThat(slot.getWidth()).isZero();
    assertThat(slot.getHeight()).isZero();
    assertThat(slot.getTtl()).isZero();
    assertThat(slot.getDisplayUrl()).isNull();
    assertThat(slot.getNativeAssets()).isNull();
  }

  @Test
  public void noBidTest() throws JSONException {
    response.put(CPM, "0");
    response.put(TTL, 0);
    Slot result = new Slot(response);
    assertEquals("0", result.getCpm());
    assertEquals(0, result.getTtl());
  }

  @Test
  public void silentModeTest() throws JSONException {
    int ttlval = 50 * 60;
    response.put(CPM, "0");
    response.put(TTL, ttlval);
    Slot result = new Slot(response);
    assertEquals("0", result.getCpm());
    assertEquals(ttlval, result.getTtl());
  }

  @Test
  public void bidCachingTest() throws JSONException {
    String cpmval = "1.5";
    int ttlval = 50 * 60;
    response.put(CPM, cpmval);
    response.put(TTL, ttlval);
    Slot result = new Slot(response);
    assertEquals(cpmval, result.getCpm());
    assertEquals(ttlval, result.getTtl());
  }

  @Test
  public void testSlot() throws JSONException {
    response.put(CPM, "10.0");
    response.put(DISPLAY_URL, "https://www.criteo.com/");
    slot = new Slot(response);
    assertTrue(slot.isValid());
  }

  @Test
  public void testSlotWithNullDisplayUrlNullCmp() {
    slot = new Slot(response);
    assertFalse(slot.isValid());
  }

  @Test
  public void testSlotWithNullDisplayUrl() throws JSONException {
    response.put(CPM, "10.0");
    slot = new Slot(response);
    assertFalse(slot.isValid());
  }

  @Test
  public void testSlotWithEmptyDisplayUrl() throws JSONException {
    response.put(CPM, "10.0");
    response.put(DISPLAY_URL, "");
    slot = new Slot(response);
    assertFalse(slot.isValid());
  }

  @Test
  public void testSlotWithNullCmp() throws JSONException {
    response.put(DISPLAY_URL, "https://www.criteo.com/");
    slot = new Slot(response);
    assertTrue(slot.isValid());
  }

  @Test
  public void testSlotWithInvalidCmp() throws JSONException {
    response.put(DISPLAY_URL, "https://www.criteo.com/");
    response.put(CPM, "abc");
    slot = new Slot(response);
    assertFalse(slot.isValid());
  }

  @Test
  public void testSlotWithNegativeValueCmp() throws JSONException {
    response.put(DISPLAY_URL, "https://www.criteo.com/");
    response.put(CPM, "-10.0");
    slot = new Slot(response);
    assertFalse(slot.isValid());
  }

  @Test
  public void testSlotWithEmptyValueCmp() throws JSONException {
    response.put(DISPLAY_URL, "https://www.criteo.com/");
    response.put(CPM, "");
    slot = new Slot(response);
    assertFalse(slot.isValid());
  }

  @Test
  public void testJsonParsingWithNative() throws Exception{
    String nativeJson = "{\n" +
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
        "                \"optoutImageUrl\": \"https://static.criteo.net/flash/icon/nai_small.png\",\n"
        +
        "                \"longLegalText\": \"\"\n" +
        "            },\n" +
        "            \"impressionPixels\": [{\n" +
        "                \"url\": \"https://cat.sv.us.criteo.com/delivery/lgn.php?\"},{\n" +
        "                \"url\": \"https://dog.da.us.criteo.com/delivery/lgn.php?\"\n" +
        "            }]\n" +
        "        }";

    String cdbStringResponse = "{\n" +
        "    \"slots\": [{\n" +
        "        \"placementId\": \"/140800857/Endeavour_Native\",\n" +
        "        \"cpm\": \"0.04\",\n" +
        "        \"currency\": \"USD\",\n" +
        "        \"width\": 2,\n" +
        "        \"height\": 2,\n" +
        "        \"ttl\": 3600,\n" +
        "        \"native\": " + nativeJson + "\n" +
        "    }]\n" +
        "}";

    JSONObject cdbResponse = new JSONObject(cdbStringResponse);
    JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
    Slot slot = new Slot(cdbSlot);

    assertEquals(NativeAssets.fromJson(new JSONObject(nativeJson)), slot.getNativeAssets());
    assertTrue(slot.isNative());
    assertTrue(slot.isValid());
  }

  @Test
  public void testParsingWithoutNative() throws Exception{
    String cdbStringResponse = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":555,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
    JSONObject cdbResponse = new JSONObject(cdbStringResponse);
    JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
    Slot slot = new Slot(cdbSlot);
    assertEquals("/140800857/Endeavour_320x50", slot.getPlacementId());
    assertEquals("1.12", slot.getCpm());
    assertEquals("EUR", slot.getCurrency());
    assertEquals(320, slot.getWidth());
    assertEquals(50, slot.getHeight());
    assertEquals(555, slot.getTtl());
    assertEquals("https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js",
        slot.getDisplayUrl());
    assertFalse(slot.isNative());
    assertNull(slot.getNativeAssets());
    assertTrue(slot.isValid());
  }

  @Test
  public void testEquality() throws Exception {
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

    JSONObject cdbResponse = new JSONObject(cdbStringResponse);
    JSONObject cdbSlot = cdbResponse.getJSONArray("slots").getJSONObject(0);
    Slot slot = new Slot(cdbSlot);
    Slot expectedSlot = new Slot(cdbSlot);
    assertEquals(expectedSlot, slot);
  }

  @Test
  public void testValidityWhenSlotIsNative() throws Exception{
    Slot slot = new Slot(getNativeJSONSlot());
    assertTrue(slot.isValid());
    assertTrue(slot.isNative());
    // if a slot claims it is native and valid then all the following conditions have to be met
    // it contains a cpm
    assertNotNull(slot.getCpm());
    assertTrue(slot.getCpm().length() > 0);
    assertTrue(slot.getCpmAsNumber() >= 0.0d);
    assertNotNull(slot.getNativeAssets());
  }

  @Test
  public void testValidity() throws Exception{
    Slot slot = new Slot(getJSONSlot());
    assertTrue(slot.isValid());
    assertFalse(slot.isNative());
    // if a slot claims it is NOT native and valid then all the following conditions have to be met
    // it contains a cpm
    assertNotNull(slot.getCpm());
    assertTrue(slot.getCpm().length() > 0);
    assertTrue(slot.getCpmAsNumber() >= 0.0d);
    assertNull(slot.getNativeAssets());
    // it contains a displayUrl
    assertNotNull(slot.getDisplayUrl());
    assertTrue(slot.getDisplayUrl().length() > 0);
    assertNotEquals("", slot.getDisplayUrl());
  }

  @Test
  public void isValid_GivenMissingDisplayUrlOrNegativeCpm_ReturnFalse() throws Exception {
    // One is missing a displayUrl and the other has a negative cpm
    // Neither bid should be added to the cache
    String json = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"0.00\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":0,\"displayUrl\":\"\"},{\"placementId\":\"/140800857/Endeavour_Interstitial_320x480\",\"cpm\":\"-1.00\",\"currency\":\"EUR\",\"width\":320,\"height\":480,\"ttl\":0,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
    List<Slot> slots = CdbResponse.fromJson(new JSONObject(json)).getSlots();

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

    List<Slot> slots = CdbResponse.fromJson(new JSONObject(cdbStringResponse)).getSlots();

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

  @Test
  public void getImpressionId_GivenAnImpressionId_ReturnIt() throws Exception {
    String json = "{\n"
        + "  \"placementId\": \"myAdUnit\",\n"
        + "  \"cpm\": \"20.00\",\n"
        + "  \"currency\": \"USD\",\n"
        + "  \"width\": 100,\n"
        + "  \"height\": 100,\n"
        + "  \"displayUrl\": \"http://criteo.com\",\n"
        + "  \"impId\": \"5e296936d48e8392e3382c45a8d9a389\"\n"
        + "}";

    Slot slot = new Slot(new JSONObject(json));

    assertThat(slot.getImpressionId()).isEqualTo("5e296936d48e8392e3382c45a8d9a389");
  }

  @Test
  public void isExpired_GivenClockBeforeExpirationPoint_ReturnFalse() throws Exception {
    Clock clock = mock(Clock.class);
    when(clock.getCurrentTimeInMillis()).thenReturn(10_000L);

    Slot slot = new Slot((getJSONSlot()));
    slot.setTtl(2);
    slot.setTimeOfDownload(9000);
    boolean expired = slot.isExpired(clock);

    assertThat(expired).isFalse();
  }

  @Test
  public void isExpired_GivenClockAfterExpirationPoint_ReturnTrue() throws Exception {
    Clock clock = mock(Clock.class);
    when(clock.getCurrentTimeInMillis()).thenReturn(10_000L);

    Slot slot = new Slot((getJSONSlot()));
    slot.setTtl(2);
    slot.setTimeOfDownload(3000);
    boolean expired = slot.isExpired(clock);

    assertThat(expired).isTrue();
  }

  @Test
  public void isExpired_GivenClockAtExpirationPoint_ReturnTrue() throws Exception {
    Clock clock = mock(Clock.class);
    when(clock.getCurrentTimeInMillis()).thenReturn(10_000L);

    Slot slot = new Slot((getJSONSlot()));
    slot.setTtl(2);
    slot.setTimeOfDownload(8000);
    boolean expired = slot.isExpired(clock);

    assertThat(expired).isTrue();
  }

  private JSONObject getNativeJSONSlot() throws Exception{
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

    JSONObject cdbResponse = new JSONObject(cdbStringResponse);
    return cdbResponse.getJSONArray("slots").getJSONObject(0);
  }

  private JSONObject getJSONSlot() throws Exception{
    String cdbStringResponse = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":555,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
    JSONObject cdbResponse = new JSONObject(cdbStringResponse);
    return cdbResponse.getJSONArray("slots").getJSONObject(0);
  }
}
