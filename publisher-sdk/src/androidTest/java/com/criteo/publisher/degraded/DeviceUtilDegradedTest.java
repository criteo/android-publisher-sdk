package com.criteo.publisher.degraded;

import static junit.framework.Assert.assertFalse;

import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.mock.MockedDependenciesRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeviceUtilDegradedTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Before
  public void setUp() throws Exception {
    DegradedUtil.assumeIsDegraded();
  }

  @Test
  public void isVersionSupported_GivenDegradedFunctionality_ReturnsFalse() throws Exception {
    DeviceUtil deviceUtil = mockedDependenciesRule.getDependencyProvider()
        .provideDeviceUtil(InstrumentationRegistry.getContext());
    boolean versionSupported = deviceUtil.isVersionSupported();
    assertFalse(versionSupported);
  }
}

