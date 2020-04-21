package com.criteo.publisher.degraded;

import static junit.framework.Assert.assertFalse;

import android.os.Build.VERSION;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.DeviceUtil;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeviceUtilDegradedTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Before
  public void setUp() throws Exception {
    assumeIsDegraded();
  }

  @Test
  public void isVersionSupported_GivenDegradedFunctionality_ReturnsFalse() throws Exception {
    DeviceUtil deviceUtil = mockedDependenciesRule.getDependencyProvider().provideDeviceUtil();
    boolean versionSupported = deviceUtil.isVersionSupported();
    assertFalse(versionSupported);
  }

  private static void assumeIsDegraded() {
    if (VERSION.SDK_INT >= 19) {
      throw new AssumptionViolatedException(
          "Functionality is not degraded, version of device should be < 19");
    }
  }
}

