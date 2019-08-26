package com.criteo.publisher.model;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void testConfigWithCriteoInit() throws CriteoInitException {
        Application app =
                (Application) InstrumentationRegistry
                        .getTargetContext()
                        .getApplicationContext();
        Criteo.init(app, "B-056946", null);
        Assert.assertNotNull(Config.getAdTagUrlMode());
        Assert.assertNotNull(Config.getDisplayUrlMacro());
        Assert.assertNotNull(Config.getAdTagDataMacro());
        Assert.assertNotNull(Config.getAdTagDataMode());
    }

    @Test
    public void testConfigWithoutCriteoInit()  {
        Assert.assertNull(Config.getAdTagUrlMode());
        Assert.assertNull(Config.getDisplayUrlMacro());
        Assert.assertNull(Config.getAdTagDataMacro());
        Assert.assertNull(Config.getAdTagDataMode());
    }

}