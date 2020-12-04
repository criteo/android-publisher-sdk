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

package com.criteo.publisher.logging

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.BuildConfigWrapper

@OpenForTesting
internal class ConsoleHandler(private val buildConfigWrapper: BuildConfigWrapper) : LogHandler {

  companion object {
    const val TagPrefix = "Crto"
    private const val UnsetLogLevel = -1
  }

  var minLogLevel: Int = UnsetLogLevel
    get() = field.takeIf { it != UnsetLogLevel } ?: buildConfigWrapper.defaultMinLogLevel

  override fun log(tag: String, logMessage: LogMessage) {
    val level = logMessage.level
    if (!isLoggable(level)) {
      return
    }

    val formattedMessage = listOfNotNull(
        logMessage.message,
        logMessage.throwable?.stacktraceString
    ).joinToString("\n")

    if (formattedMessage.isNotEmpty()) {
      println(level, tag, formattedMessage)
    }
  }

  @VisibleForTesting
  fun println(level: Int, tag: String, message: String) {
    Log.println(level, "$TagPrefix$tag", message)
  }

  private val Throwable.stacktraceString get() = getStackTraceString(this)

  /**
   * This method is nullable because on JVM tests, methods from AndroidSDK returns null
   */
  @VisibleForTesting
  fun getStackTraceString(throwable: Throwable): String? = Log.getStackTraceString(throwable)

  private fun isLoggable(level: Int): Boolean {
    return level >= minLogLevel
  }
}
