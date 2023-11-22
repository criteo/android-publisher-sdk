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

package com.criteo.publisher.adview

import android.app.Activity
import android.content.pm.ActivityInfo

enum class MraidOrientation(val value: String) {
  PORTRAIT("portrait"),
  LANDSCAPE("landscape"),
  NONE("none"),
}

internal fun String?.asMraidOrientation(): MraidOrientation {
  return MraidOrientation.values().firstOrNull { it.value == this }
      ?: MraidOrientation.NONE
}

internal fun Activity.setRequestedOrientation(
    allowOrientationChange: Boolean,
    forceOrientation: MraidOrientation
) {
  if (forceOrientation === MraidOrientation.LANDSCAPE) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
  }
  if (forceOrientation === MraidOrientation.PORTRAIT) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
  }
  if (forceOrientation === MraidOrientation.NONE && !allowOrientationChange) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  if (allowOrientationChange) {
    // unlock orientation lock after forced rotation
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }
}
