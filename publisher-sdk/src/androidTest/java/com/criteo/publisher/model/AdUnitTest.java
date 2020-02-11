package com.criteo.publisher.model;

import static org.junit.Assert.assertEquals;

import com.criteo.publisher.Util.AdUnitType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AdUnitTest {

  private static final String PLACEMENT_ID = "placementId";
  private static final String SIZES = "sizes";
  private static final String PLACEMENT_ID_VALUE = "/140800857/Endeavour_320x50";
  private static final int HEIGHT = 10;
  private static final int WIDTH = 350;
  private AdSize adSize;
  private CacheAdUnit cacheAdUnit;

  @Before
  public void initialize() {
    adSize = new AdSize(WIDTH, HEIGHT);
    cacheAdUnit = new CacheAdUnit(adSize, PLACEMENT_ID_VALUE, AdUnitType.CRITEO_BANNER);
  }

  @Test
  public void testAdUnitJsonObject() throws JSONException {
    JSONObject adUnitJson = cacheAdUnit.toJson();
    assertEquals(PLACEMENT_ID_VALUE, adUnitJson.get(PLACEMENT_ID));
    JSONArray adUnitSizes = (JSONArray) adUnitJson.get(SIZES);
    assertEquals(adSize.getFormattedSize(), adUnitSizes.getString(0));
  }

}