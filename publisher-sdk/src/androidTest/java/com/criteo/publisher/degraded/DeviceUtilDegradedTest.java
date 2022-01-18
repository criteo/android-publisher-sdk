/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.degraded;

import static org.junit.Assert.assertFalse;

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

