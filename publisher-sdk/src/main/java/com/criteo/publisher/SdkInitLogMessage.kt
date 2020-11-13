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
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.model.AdUnit

internal object SdkInitLogMessage {

  @JvmStatic
  fun onSdkInitialized(
      cpId: String,
      adUnits: List<AdUnit>,
      version: String
  ) = LogMessage(message =
    """Criteo SDK version $version is initialized with Publisher ID $cpId and ${adUnits.size} ad units:
${adUnits.joinToString("\n") { "- $it" }}"""
  )

  @JvmStatic
  fun onSdkInitializedMoreThanOnce() = LogMessage(Log.WARN, message =
    "Criteo SDK initialization method cannot be called more than once"
  )
}
