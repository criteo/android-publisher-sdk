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

package com.criteo.publisher

import android.util.Log
import com.criteo.publisher.logging.CallerInferrer
import com.criteo.publisher.logging.LogMessage

internal object ErrorLogMessage {

  @JvmStatic
  @CallerInferrer.Transparent
  @Suppress("NOTHING_TO_INLINE")
  inline fun onUncaughtErrorAtPublicApi(throwable: Throwable) = LogMessage(
      Log.ERROR,
      "Internal error in ${CallerInferrer.inferCallerName()}",
      throwable,
      "onUncaughtErrorAtPublicApi"
  )

  @JvmStatic
  fun onUncaughtErrorInThread(throwable: Throwable) = LogMessage(
      Log.ERROR,
      "Uncaught error in thread",
      throwable,
      "onUncaughtErrorInThread"
  )

  @JvmStatic
  fun onAssertFailed(throwable: Throwable) = LogMessage(
      Log.ERROR,
      "Assertion failed",
      throwable,
      "onAssertFailed"
  )
}
