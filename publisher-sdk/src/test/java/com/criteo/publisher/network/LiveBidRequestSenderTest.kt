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

import com.criteo.publisher.Clock
import com.criteo.publisher.LiveCdbCallListener
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.model.CacheAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbRequestFactory
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.model.Config
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future

class LiveBidRequestSenderTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var pubSdkApi: PubSdkApi

  @Mock
  private lateinit var cdbRequestFactory: CdbRequestFactory

  @Mock
  private lateinit var config: Config

  @Mock
  private lateinit var cacheAdUnit: CacheAdUnit

  @Mock
  private lateinit var contextData: ContextData

  @Mock
  private lateinit var liveCdbCallListener: LiveCdbCallListener

  @Mock
  private lateinit var cdbRequest: CdbRequest

  @Mock
  private lateinit var cdbResponse: CdbResponse

  @Mock
  private lateinit var userAgentFuture: Future<String>

  @Mock
  private lateinit var clock: Clock

  @Test
  fun timeBudgetTimerKicksOff_ThenTimeBudgetExceededTrigger() {
    whenever(cdbRequestFactory.userAgent).thenReturn(userAgentFuture)
    whenever(userAgentFuture.get()).thenReturn("fake_user_agent")
    whenever(cdbRequestFactory.createRequest(eq(cacheAdUnit), eq(contextData))).thenReturn(cdbRequest)
    whenever(pubSdkApi.loadCdb(eq(cdbRequest), any())).thenReturn(cdbResponse)
    whenever(config.liveBiddingTimeBudgetInMillis).thenReturn(1)

    val liveBidRequestSender = LiveBidRequestSender(
        pubSdkApi,
        cdbRequestFactory,
        clock,
        getDelayedExecutor(config.liveBiddingTimeBudgetInMillis.toLong() + 100),
        getScheduledExecutorService(),
        config
    )

    liveBidRequestSender.sendLiveBidRequest(
        cacheAdUnit,
        contextData,
        liveCdbCallListener
    )

    verify(liveCdbCallListener).onTimeBudgetExceeded()
  }

  private fun getDelayedExecutor(delayInMillis: Long) =
      Executor {
        Thread.sleep(delayInMillis)
        it.run()
      }

  private fun getScheduledExecutorService() = Executors.newSingleThreadScheduledExecutor()
}
