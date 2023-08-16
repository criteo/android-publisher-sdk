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
package com.criteo.publisher.util

import android.content.pm.ActivityInfo
import android.os.Build
import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.test.activity.DummyActivity
import com.criteo.publisher.util.AndroidUtilTest.setOrientationAndWait
import org.assertj.core.api.Assertions.assertThat
import org.junit.AssumptionViolatedException
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class DeviceUtilIntegrationTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val activityRule = ActivityTestRule(DummyActivity::class.java)

  @Inject
  private lateinit var deviceUtil: DeviceUtil

  @Test
  fun isVersionSupported_GivenDeviceAboveOrEqual19_ReturnsTrue() {
    if (Build.VERSION.SDK_INT < 19) {
      throw AssumptionViolatedException("Version of device should be >= 19")
    }

    val versionSupported = deviceUtil.isVersionSupported()

    assertThat(versionSupported).isTrue()
  }

  @Test
  fun getCurrentScreenSize_GivenOrientationChange_ReturnDifferentScreenSize() {
    setOrientationAndWait(activityRule, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val portraitSize = deviceUtil.getCurrentScreenSize()

    setOrientationAndWait(activityRule, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    val landscapeSize = deviceUtil.getCurrentScreenSize()

    assertThat(portraitSize).isNotEqualTo(landscapeSize)
  }

}
