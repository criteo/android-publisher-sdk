package com.criteo.publisher.Util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Build.VERSION;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceUtilIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private DeviceUtil deviceUtil;

  @Before
  public void setup() {
    deviceUtil = mockedDependenciesRule.getDependencyProvider()
        .provideDeviceUtil(InstrumentationRegistry.getContext());
  }

  // TODO Create Intrumentation Test , change settings as Limited and test

  @Test
  public void getDeviceModelTest() {
    assertNotNull(deviceUtil.getDeviceModel());
  }

  @Test
  public void isVersionSupported_GivenDeviceAboveOrEqual19_ReturnsTrue() {
    if (VERSION.SDK_INT < 19) {
      throw new AssumptionViolatedException("Version of device should be >= 19");
    }

    boolean versionSupported = deviceUtil.isVersionSupported();

    assertTrue(versionSupported);
  }
}

