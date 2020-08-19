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

package com.criteo.publisher.network

import com.criteo.publisher.LiveCdbCallListener
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.model.CacheAdUnit
import com.criteo.publisher.model.CdbRequestFactory
import java.util.concurrent.Executor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@OpenForTesting
class LiveBidRequestSender(
    private val pubSdkApi: PubSdkApi,
    private val cdbRequestFactory: CdbRequestFactory,
    private val executor: Executor,
    private val scheduledExecutorService: ScheduledExecutorService
) {
  companion object {
    const val DEFAULT_TIME_BUDGET_IN_MILLIS = 5_000L
  }

  fun sendLiveBidRequest(
      cacheAdUnit: CacheAdUnit,
      liveCdbCallListener: LiveCdbCallListener
  ) {
    scheduledExecutorService.schedule({
      liveCdbCallListener.onTimeBudgetExceeded()
    }, DEFAULT_TIME_BUDGET_IN_MILLIS, TimeUnit.MILLISECONDS)

    executor.execute(
        CdbCall(
            pubSdkApi,
            cdbRequestFactory,
            listOf(cacheAdUnit),
            liveCdbCallListener
        )
    )
  }
}
