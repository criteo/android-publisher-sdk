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

import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.bid.UniqueIdGenerator

@OpenForTesting
internal class Session(
    private val clock: Clock,
    private val uniqueIdGenerator: UniqueIdGenerator
) {
  companion object {
    const val MILLIS_IN_SECOND = 1000
  }

  private val startingTime = clock.currentTimeInMillis

  /**
   * Return a unique ID for this session.
   */
  val sessionId: String by lazy {
    uniqueIdGenerator.generateId()
  }

  /**
   * Returns the current time in seconds since the initialization of the SDK.
   *
   * @return the difference, measured in seconds, between the current time and the SDK init.
   */
  fun getDurationInSeconds(): Int = ((clock.currentTimeInMillis - startingTime) / MILLIS_IN_SECOND).toInt()
}
