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

internal object LogTag {
  private const val TagPrefix = "CriteoSdk"

  /**
   * IllegalArgumentException is thrown if the tag.length() > 23 for Nougat (7.0)
   * releases (API <= 23) and prior, there is no tag limit of concern after this API level.
   *
   * @see [documentation](https://developer.android.com/reference/android/util/Log.html)
   */
  private const val TagMaxLength = 23

  @JvmStatic
  fun with(str: String) = "$TagPrefix$str".take(TagMaxLength)
}
