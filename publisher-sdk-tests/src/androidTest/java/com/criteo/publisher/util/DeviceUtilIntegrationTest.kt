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

package com.criteo.publisher.util;

import static org.junit.Assert.assertTrue;

import android.os.Build.VERSION;
import androidx.test.runner.AndroidJUnit4;
import com.criteo.publisher.mock.MockedDependenciesRule;
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
    deviceUtil = mockedDependenciesRule.getDependencyProvider().provideDeviceUtil();
  }

  // TODO Create Intrumentation Test , change settings as Limited and test

  @Test
  public void isVersionSupported_GivenDeviceAboveOrEqual19_ReturnsTrue() {
    if (VERSION.SDK_INT < 19) {
      throw new AssumptionViolatedException("Version of device should be >= 19");
    }

    boolean versionSupported = deviceUtil.isVersionSupported();

    assertTrue(versionSupported);
  }
}

