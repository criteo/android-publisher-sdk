package com.criteo.publisher.model;

import com.google.android.gms.ads.InterstitialAd;

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

}