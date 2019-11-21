package com.criteo.publisher.degraded;

import static junit.framework.Assert.assertFalse;

import com.criteo.publisher.Util.DeviceUtil;
import org.junit.Before;
import org.junit.Test;

public class DeviceUtilDegradedTest {

  @Before
  public void setUp() throws Exception {
    DegradedUtil.assumeIsDegraded();
  }

  @Test
  public void isVersionSupported_GivenDegradedFunctionality_ReturnsFalse() throws Exception {
    boolean versionSupported = DeviceUtil.isVersionSupported();

    assertFalse(versionSupported);
  }

}
