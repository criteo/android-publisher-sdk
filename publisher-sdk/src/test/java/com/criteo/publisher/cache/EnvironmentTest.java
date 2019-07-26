package com.criteo.publisher.cache;

import com.criteo.publisher.Util.DeviceUtil;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvironmentTest {
    @Test
    @Ignore("Half baked feature that isn't used")
    public void getEnviorementVairableTest() {
        assertEquals(true, DeviceUtil.isLoggingEnabled());
    }
}
