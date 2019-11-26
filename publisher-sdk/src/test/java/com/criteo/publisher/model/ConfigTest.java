package com.criteo.publisher.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ConfigTest {

    private Config config;
    private Context mockContext;

    @Test
    public void testConfigInit() {
        mockContext = mock(Context.class);
        config = new Config(mockContext);
        Assert.assertFalse(config.isKillSwitchEnabled());
        Assert.assertNotNull(config.getAdTagUrlMode());
        Assert.assertNotNull(config.getDisplayUrlMacro());
        Assert.assertNotNull(config.getAdTagDataMacro());
        Assert.assertNotNull(config.getAdTagDataMode());
    }

    @Test
    public void testConfigInitWithCachedKillSwitch() {
        mockContext = mock(Context.class);

        SharedPreferences mockSharedPref = mock(SharedPreferences.class);
        when(mockSharedPref.getBoolean("CriteoCachedKillSwitch", false)).thenReturn(true);
        when(mockContext.getString(Mockito.any(int.class))).thenReturn("sharedPref");
        when(mockContext.getSharedPreferences(
                Mockito.any(String.class), Mockito.any(int.class))).thenReturn(mockSharedPref);

        config = new Config(mockContext);

        Assert.assertTrue(config.isKillSwitchEnabled());
        Assert.assertNotNull(config.getAdTagUrlMode());
        Assert.assertNotNull(config.getDisplayUrlMacro());
        Assert.assertNotNull(config.getAdTagDataMacro());
        Assert.assertNotNull(config.getAdTagDataMode());
    }

    @Test
    public void testRefreshConfig() {
        mockContext = mock(Context.class);

        SharedPreferences mockSharedPref = mock(SharedPreferences.class);
        when(mockSharedPref.getBoolean("CriteoCachedKillSwitch", false)).thenReturn(false);
        when(mockContext.getString(Mockito.any(int.class))).thenReturn("sharedPref");
        when(mockContext.getSharedPreferences(
                Mockito.any(String.class), Mockito.any(int.class))).thenReturn(mockSharedPref);

        config = new Config(mockContext);

        String oldDisplayUrlMacro = "%%displayUrl%%";
        String newDisplayUrlMacro = "%%newDisplayUrl%%";
        JSONObject json = new JSONObject();
        try {
            json.put("killSwitch", true);
            json.put("AndroidDisplayUrlMacro", newDisplayUrlMacro);

        } catch (JSONException je) {
            Assert.fail("JSON exception" + je.getMessage());
        }

        config.refreshConfig(json);

        Assert.assertTrue(config.isKillSwitchEnabled());
        Assert.assertNotNull(config.getAdTagUrlMode());
        Assert.assertEquals(newDisplayUrlMacro, config.getDisplayUrlMacro());
        Assert.assertNotEquals(oldDisplayUrlMacro, config.getDisplayUrlMacro());
        Assert.assertNotNull(config.getAdTagDataMacro());
        Assert.assertNotNull(config.getAdTagDataMode());
    }

}