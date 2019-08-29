package com.criteo.publisher.model;

import org.junit.Assert;
import org.junit.Test;

public class CacheAdUnitTest {

    @Test
    public void checkFormattedSize() {
        CacheAdUnit cacheAdUnit = new CacheAdUnit(new AdSize(320, 50), "AdUnitId");
        Assert.assertTrue(cacheAdUnit.getFormattedSize().equals("320x50"));
    }

}