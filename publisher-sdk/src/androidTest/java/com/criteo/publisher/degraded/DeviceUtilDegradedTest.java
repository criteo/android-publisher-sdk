package com.criteo.publisher.degraded;

import static junit.framework.Assert.assertTrue;

import com.criteo.publisher.Util.DeviceUtil;
import org.junit.Before;
import org.junit.Test;

public class DeviceUtilDegradedTest {

  @Before
  public void setUp() throws Exception {
    DegradedUtil.assumeIsDegraded();
  }

  @Test
  public void isVersionNotSupported_GivenDegradedFunctionality_ReturnsTrue() throws Exception {
    boolean versionNotSupported = DeviceUtil.isVersionNotSupported();

    assertTrue(versionNotSupported);
  }

}
