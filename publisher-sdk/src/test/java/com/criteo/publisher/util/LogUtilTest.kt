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

import android.util.Log
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LogUtilTest {
  @Test
  fun asAndroidLogLevel_whenExistingLogLevelIsPassed_ShouldReturnAndroidLogLevelValue() {
    val correctLogLevelsMapping = listOf(
        "Debug" to Log.DEBUG,
        "Info" to Log.INFO,
        "Warning" to Log.WARN,
        "Error" to Log.ERROR
    )

    correctLogLevelsMapping.forEach {
      val androidLogLevel = it.first.asAndroidLogLevel()

      assertThat(androidLogLevel).isEqualTo(it.second)
    }
  }

  @Test
  fun asAndroidLogLevel_whenRandomStringIsPassed_ShouldReturnNull() {
    val randomStrings = listOf("DEBug", "Hello", "TestString", "")

    randomStrings.forEach {
      val androidLogLevel = it.asAndroidLogLevel()
      assertThat(androidLogLevel).isNull()
    }
  }
}
