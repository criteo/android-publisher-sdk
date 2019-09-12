package com.criteo.publisher.model;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class CacheAdUnitTest {

    @Test
    public void checkFormattedSize() {
        CacheAdUnit cacheAdUnit = new CacheAdUnit(new AdSize(320, 50), "AdUnitId", false);
        Assert.assertEquals(cacheAdUnit.getSize().getWidth(), 320);
        Assert.assertEquals(cacheAdUnit.getSize().getHeight(), 50);
    }

    @Test
    public void checkHashCode() {
        CacheAdUnit cacheAdUnit = new CacheAdUnit(new AdSize(320, 50), "AdUnitId", false);
        CacheAdUnit anotherCacheAdUnit = new CacheAdUnit(new AdSize(320, 50), "AdUnitId", false);
        Assert.assertEquals(cacheAdUnit.hashCode(), anotherCacheAdUnit.hashCode());
    }

    @Test
    public void checkHashCodeWithSameId() {
        CacheAdUnit cacheAdUnit = new CacheAdUnit(new AdSize(320, 480), "AdUnitId", false);
        CacheAdUnit anotherCacheAdUnit = new CacheAdUnit(new AdSize(320, 480), "AdUnitId", true);
        Assert.assertNotEquals(cacheAdUnit.hashCode(), anotherCacheAdUnit.hashCode());
    }

    @Test
    public void testToJson() {
        try {
            AdSize adSize = new AdSize(320, 480);
            CacheAdUnit cacheAdUnit = new CacheAdUnit(adSize, "AdUnitId", false);
            JSONObject json = cacheAdUnit.toJson();
            Assert.assertTrue(json.has("placementId"));
            Assert.assertEquals("AdUnitId", json.getString("placementId"));
            Assert.assertTrue(json.has("sizes"));
            Assert.assertEquals(1, json.getJSONArray("sizes").length());
            Assert.assertEquals(adSize.getFormattedSize(), json.getJSONArray("sizes").getString(0));
            Assert.assertFalse(json.has("isNative"));

        } catch (Exception ex) {
            Assert.fail("Error converting to json : "+ ex.getLocalizedMessage());
        }
    }

    @Test
    public void testNativeToJson() {
        try {
            AdSize adSize = new AdSize(2, 2);
            CacheAdUnit nativeCacheAdUnit = new CacheAdUnit(adSize, "AdUnitId", true);
            JSONObject nativeJson = nativeCacheAdUnit.toJson();
            Assert.assertTrue(nativeJson.has("placementId"));
            Assert.assertEquals("AdUnitId", nativeJson.getString("placementId"));
            Assert.assertTrue(nativeJson.has("sizes"));
            Assert.assertEquals(1, nativeJson.getJSONArray("sizes").length());
            Assert.assertEquals(adSize.getFormattedSize(), nativeJson.getJSONArray("sizes").getString(0));
            Assert.assertTrue(nativeJson.has("isNative"));
            Assert.assertTrue(nativeJson.getBoolean("isNative"));

        } catch (Exception ex) {
            Assert.fail("Error converting to json : "+ ex.getLocalizedMessage());
        }
    }
}