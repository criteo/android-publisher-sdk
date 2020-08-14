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
import com.criteo.publisher.BidPrefetchRateLimiter.Companion.STORAGE_KEY
import com.criteo.publisher.BidPrefetchRateLimiter.Companion.UNSET_PREFETCH_TIME
import com.criteo.publisher.model.Config
import com.criteo.publisher.util.SafeSharedPreferences
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BidPrefetchRateLimiterTest {

  @Mock
  private lateinit var config: Config

  @Mock
  private lateinit var clock: Clock

  @Mock
  private lateinit var safeSharedPreferences: SafeSharedPreferences

  @Mock
  private lateinit var sharedPreferences: SharedPreferences

  @InjectMocks
  private lateinit var bidPrefetchRateLimiter: BidPrefetchRateLimiter

  @Mock
  private lateinit var editor: SharedPreferences.Editor

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun canPrefetch_givenFirstCallToBidPrefetch_ThenReturnTrue() {
    whenever(safeSharedPreferences.getLong(STORAGE_KEY, UNSET_PREFETCH_TIME)).thenReturn(UNSET_PREFETCH_TIME)

    assertTrue {
      bidPrefetchRateLimiter.canPrefetch()
    }
  }

  @Test
  fun canPrefetch_givenImmediateSecondCallToBidPrefetch_ThenReturnFalse() {
    whenever(safeSharedPreferences.getLong(STORAGE_KEY, UNSET_PREFETCH_TIME)).thenReturn(1L)
    whenever(clock.currentTimeInMillis).thenReturn(2L)
    whenever(config.minTimeBetweenPrefetchesMillis).thenReturn(2L)

    assertFalse {
      bidPrefetchRateLimiter.canPrefetch()
    }
  }

  @Test
  fun canPrefetch_givenNotImmediateSecondCallToBidPrefetch_ThenReturnFalse() {
    whenever(safeSharedPreferences.getLong(STORAGE_KEY, UNSET_PREFETCH_TIME)).thenReturn(1L)
    whenever(clock.currentTimeInMillis).thenReturn(3L)
    whenever(config.minTimeBetweenPrefetchesMillis).thenReturn(2L)

    assertTrue {
      bidPrefetchRateLimiter.canPrefetch()
    }
  }

  @Test
  fun setPrefetchTime() {
    whenever(sharedPreferences.edit()).thenReturn(editor)
    whenever(clock.currentTimeInMillis).thenReturn(1L)

    bidPrefetchRateLimiter.setPrefetchTime()

    verify(editor).putLong(STORAGE_KEY, 1L)
    verify(editor).apply()
  }
}
