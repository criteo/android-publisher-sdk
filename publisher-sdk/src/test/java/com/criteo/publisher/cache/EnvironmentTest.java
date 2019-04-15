package com.criteo.publisher.cache;

import com.criteo.publisher.Util.DeviceUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvironmentTest {
    @Test
    public void getEnviorementVairableTest() {
        assertEquals(true, DeviceUtil.isLoggingEnabled());
    }
}
