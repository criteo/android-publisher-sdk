package com.criteo.publisher.model;

import static org.junit.Assert.*;

import android.content.Context;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    private Config config;

    @Test
    public void testEmptyConfig(){
        JSONObject configJson = new JSONObject();
        config = new Config(configJson);
        Assert.assertNotNull(config.isKillSwitch());
        Assert.assertNotNull(config.getAdTagUrlMode());
        Assert.assertNotNull(config.getDisplayUrlMacro());
        Assert.assertNotNull(config.getAdTagDataMacro());
        Assert.assertNotNull(config.getAdTagDataMode());
    }

}