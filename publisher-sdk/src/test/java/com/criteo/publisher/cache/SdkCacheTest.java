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

package com.criteo.publisher.cache;

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.DeviceUtil;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SdkCacheTest {

  @Mock
  private Context context;

  @Mock
  private AdvertisingInfo advertisingInfo;

  private JSONArray slots;
  private DeviceUtil deviceUtil;
  private SdkCache cache;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    deviceUtil = new DeviceUtil(context, advertisingInfo);
    cache = new SdkCache(deviceUtil);
  }

  @Test
  public void cacheSize() throws JSONException {
    initializeCache();
    assertEquals(slots.length(), cache.getItemCount());
  }

  @Test
  public void getAdUnitFromCacheTest() throws JSONException {
    initializeCache();

    for (int i = slots.length() - 1; i >= 0; i--) {
      String placement = slots.getJSONObject(i).getString("placementId");
      AdSize adSize = new AdSize(slots.getJSONObject(i).getInt("width"),
          slots.getJSONObject(i).getInt("height"));
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
  public void getNotCachedAdUnitFromCacheTest() throws JSONException {
    initializeCache();

    String placement = "this/isnt/in/the/cache";
    AdSize adSize = new AdSize(320, 50);
    CacheAdUnit testAdUnit = new CacheAdUnit(adSize, placement, CRITEO_BANNER);
    Slot cachedSlot = cache.peekAdUnit(testAdUnit);
    assertNull(cachedSlot);
  }

  @Test
  public void peekAdUnitFromCacheTest() throws JSONException {
    initializeCache();

    for (int i = slots.length() - 1; i >= 0; i--) {
      String placement = slots.getJSONObject(i).getString("placementId");
      AdSize adSize = new AdSize(slots.getJSONObject(i).getInt("width"),
          slots.getJSONObject(i).getInt("height"));
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

    for (int i = slots.length() - 1; i >= 0; i--) {
      String placement = slots.getJSONObject(i).getString("placementId");
      AdSize adSize = new AdSize(slots.getJSONObject(i).getInt("width"),
          slots.getJSONObject(i).getInt("height"));
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
    cache = new SdkCache(deviceUtil);
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
      cache = new SdkCache(deviceUtil);
      cache.add(slot);
      assertEquals(1, cache.getItemCount());
    } catch (Exception ex) {
      Assert.fail("Json exception in test data : " + ex.getLocalizedMessage());
    }
  }

  @Test
  public void add_GivenValidNativeSlot_AddItInCache() {
    AdSize size = new AdSize(1, 2);
    Slot slot = givenNativeSlot(size, "myAdUnit");

    CacheAdUnit expectedKey = new CacheAdUnit(size, "myAdUnit", CRITEO_CUSTOM_NATIVE);

    cache.add(slot);
    Slot adUnit = cache.peekAdUnit(expectedKey);

    assertThat(adUnit).isSameAs(slot);
  }

  @Test
  public void add_GivenValidBannerSlot_AddItInCache() {
    AdSize size = new AdSize(1, 2);

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getWidth()).thenReturn(size.getWidth());
    when(slot.getHeight()).thenReturn(size.getHeight());
    when(slot.getPlacementId()).thenReturn("myAdUnit");

    CacheAdUnit expectedKey = new CacheAdUnit(size, "myAdUnit", CRITEO_BANNER);

    cache.add(slot);
    Slot adUnit = cache.peekAdUnit(expectedKey);

    assertThat(adUnit).isSameAs(slot);
  }

  @Test
  public void add_GivenValidInterstitialSlotInPortrait_AddItInCache() {
    AdSize size = new AdSize(300, 400);

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getWidth()).thenReturn(size.getWidth());
    when(slot.getHeight()).thenReturn(size.getHeight());
    when(slot.getPlacementId()).thenReturn("myAdUnit");

    // FIXME this has side-effect. After fixing EE-608, this will have no meaning.
    deviceUtil.setScreenSize(size.getWidth(), size.getHeight());

    CacheAdUnit expectedKey = new CacheAdUnit(size, "myAdUnit", CRITEO_INTERSTITIAL);

    cache.add(slot);
    Slot adUnit = cache.peekAdUnit(expectedKey);

    assertThat(adUnit).isSameAs(slot);
  }

  @Test
  public void add_GivenValidInterstitialSlotInLandscape_AddItInCache() {
    AdSize size = new AdSize(400, 300);

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getWidth()).thenReturn(size.getWidth());
    when(slot.getHeight()).thenReturn(size.getHeight());
    when(slot.getPlacementId()).thenReturn("myAdUnit");

    // FIXME this has side-effect. After fixing EE-608, this will have no meaning.
    deviceUtil.setScreenSize(size.getHeight(), size.getWidth());

    CacheAdUnit expectedKey = new CacheAdUnit(size, "myAdUnit", CRITEO_INTERSTITIAL);

    cache.add(slot);
    Slot adUnit = cache.peekAdUnit(expectedKey);

    assertThat(adUnit).isSameAs(slot);
  }

  @Test
  public void peekAdUnit_PeekingTwiceExistingSlot_ReturnTwiceTheSameSlotWithoutRemovingIt()
      throws Exception {
    AdSize size = new AdSize(1, 2);
    Slot slot = givenNativeSlot(size, "myAdUnit");
    CacheAdUnit key = new CacheAdUnit(size, "myAdUnit", CRITEO_CUSTOM_NATIVE);

    cache.add(slot);
    Slot slot1 = cache.peekAdUnit(key);
    Slot slot2 = cache.peekAdUnit(key);

    assertThat(cache.getItemCount()).isEqualTo(1);
    assertThat(slot1).isSameAs(slot);
    assertThat(slot2).isSameAs(slot);
  }

  @Test
  public void peekAdUnit_PeekingNonExistingSlot_ReturnNull() throws Exception {
    AdSize size = new AdSize(1, 2);
    CacheAdUnit key = new CacheAdUnit(size, "myAdUnit", CRITEO_CUSTOM_NATIVE);

    Slot slot = cache.peekAdUnit(key);

    assertThat(slot).isNull();
  }

  private static Slot givenNativeSlot(AdSize size, String placementId) {
    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(true);
    when(slot.getWidth()).thenReturn(size.getWidth());
    when(slot.getHeight()).thenReturn(size.getHeight());
    when(slot.getPlacementId()).thenReturn(placementId);

    return slot;
  }
}
