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

import android.content.SharedPreferences
import com.criteo.publisher.annotation.Internal
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.model.Config
import com.criteo.publisher.util.SafeSharedPreferences

/**
 * This class guards against calling [BidManager.prefetch] many times in a short time span. This protects
 * against apps with services that are re-spawned automatically (on boot, after process kill) which
 * could cause too many initializations of [Criteo] in a short amount of time.
 */
@OpenForTesting
@Internal
class BidPrefetchRateLimiter(
    private val clock: Clock,
    private val sharedPreferences: SharedPreferences,
    private val safeSharedPreferences: SafeSharedPreferences,
    private val config: Config
) {

  companion object {
    const val STORAGE_KEY = "LAST_PREFETCH_TIME"
    const val UNSET_PREFETCH_TIME = -1L
  }

  fun canPrefetch(): Boolean {
    val lastPrefetchTime: Long = safeSharedPreferences.getLong(STORAGE_KEY, UNSET_PREFETCH_TIME)
    if (lastPrefetchTime == UNSET_PREFETCH_TIME ||
        clock.currentTimeInMillis - lastPrefetchTime >= config.minTimeBetweenPrefetchesMillis) {
      return true
    }
    return false
  }

  fun setPrefetchTime() {
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putLong(STORAGE_KEY, clock.currentTimeInMillis)
    editor.apply()
  }
}
