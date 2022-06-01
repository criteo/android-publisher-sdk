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

import com.criteo.publisher.CdbCallListener
import com.criteo.publisher.Clock
import com.criteo.publisher.concurrent.DirectMockExecutor
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.CacheAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbRequestFactory
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.model.Config
import com.criteo.publisher.model.RemoteConfigRequest
import com.criteo.publisher.model.RemoteConfigRequestFactory
import com.criteo.publisher.model.RemoteConfigResponse
import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.util.CompletableFuture.completedFuture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class BidRequestSenderTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var cdbRequestFactory: CdbRequestFactory

    @Mock
    private lateinit var remoteConfigRequestFactory: RemoteConfigRequestFactory

    @Mock
    private lateinit var clock: Clock

    @Mock
    private lateinit var api: PubSdkApi

    private var executor = Executor(Runnable::run)

    private lateinit var sender: BidRequestSender

    private val adUnitId = AtomicInteger(0)

    @Before
    fun setUp() {
        whenever(cdbRequestFactory.userAgent).doReturn(completedFuture(""))

        givenNewSender()
    }

    private fun givenNewSender(executor: Executor = this.executor) {
        sender = BidRequestSender(
            cdbRequestFactory,
            remoteConfigRequestFactory,
            clock,
            api,
            executor
        )
    }

    @Test
    fun sendRemoteConfigRequest_GivenSuccessfulResponse_RefreshConfig() {
        val configToUpdate: Config = mock()
        val request: RemoteConfigRequest = mock()
        val response: RemoteConfigResponse = mock()

        whenever(remoteConfigRequestFactory.createRequest()).doReturn(request)
        whenever(api.loadConfig(request)).doReturn(response)

        sender.sendRemoteConfigRequest(configToUpdate)

        verify(configToUpdate).refreshConfig(response)
    }

    @Test
    fun sendRemoteConfigRequest_GivenException_DoNotThrow() {
        val configToUpdate: Config = mock()
        whenever(api.loadConfig(any())).doThrow(IOException::class)

        assertThatCode {
            sender.sendRemoteConfigRequest(configToUpdate)
        }.doesNotThrowAnyException()
    }

    @Test
    fun sendRemoteConfigRequest_GivenExecutor_IsWorkingInExecutor() {
        val executor = DirectMockExecutor()
        givenNewSender(executor = executor)

        doAnswer {
            executor.expectIsRunningInExecutor()
            null
        }.whenever(api).loadConfig(anyOrNull())

        sender.sendRemoteConfigRequest(mock())

        executor.verifyExpectations()
    }

    @Test
    fun sendBidRequest_GivenAdUnitAndSuccessfulResponse_NotifyListener() {
        val adUnit = createAdUnit()
        val contextData: ContextData = mock()
        val listener: CdbCallListener = mock()
        val request: CdbRequest = mock()
        val userAgent = "myUserAgent"
        val response: CdbResponse = mock()

        cdbRequestFactory.stub {
            on { createRequest(adUnit, contextData) } doReturn request
            on { it.userAgent } doReturn completedFuture(userAgent)
        }

        whenever(api.loadCdb(request, userAgent)).doReturn(response)

        sender.sendBidRequest(adUnit, contextData, listener)

        val inOrder = inOrder(listener)
        inOrder.verify(listener).onCdbRequest(request)
        inOrder.verify(listener).onCdbResponse(request, response)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun sendBidRequest_GivenAdUnitAndError_NotifyListener() {
        val adUnit = createAdUnit()
        val contextData: ContextData = mock()
        val listener: CdbCallListener = mock()
        val request: CdbRequest = mock()
        val userAgent = "myUserAgent"
        val exception = IOException("")

        cdbRequestFactory.stub {
            on { createRequest(adUnit, contextData) } doReturn request
            on { it.userAgent } doReturn completedFuture(userAgent)
        }

        whenever(api.loadCdb(request, userAgent)).doThrow(exception)

        sender.sendBidRequest(adUnit, contextData, listener)

        val inOrder = inOrder(listener)
        inOrder.verify(listener).onCdbRequest(request)
        inOrder.verify(listener).onCdbError(request, exception)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun sendBidRequest_GivenExecutor_IsWorkingInExecutor() {
        val executor = DirectMockExecutor()
        givenNewSender(executor = executor)

        doAnswer {
            executor.expectIsRunningInExecutor()
            null
        }.whenever(api).loadCdb(anyOrNull(), anyOrNull())

        sender.sendBidRequest(createAdUnit(), mock(), mock())

        executor.verifyExpectations()
    }

    @Test
    fun sendBidRequest_GivenTwoDifferentAdUnitsInParallel_ExecuteThem() {
        executor = Executors.newFixedThreadPool(2)
        givenNewSender()

        // 3 parties because 2 are in API call, why the 3rd is this test assertion
        val bothRequestsAreInParallel = CyclicBarrier(3)
        doAnswer {
            bothRequestsAreInParallel.await()
            null
        }.whenever(api).loadCdb(anyOrNull(), anyOrNull())

        sender.sendBidRequest(createAdUnit(), mock(), mock())
        sender.sendBidRequest(createAdUnit(), mock(), mock())

        bothRequestsAreInParallel.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun sendBidRequest_GivenTwoSameAdUnitsInParallel_ExecuteOnlyTheFirstOne() {
        executor = spy(Executors.newFixedThreadPool(2))
        givenNewSender()

        whenever(api.loadCdb(any(), any())).doReturn(mock())

        val adUnit = createAdUnit()

        sender.sendBidRequest(adUnit, mock(), mock())
        sender.sendBidRequest(adUnit, mock(), mock())

        verify(executor, times(1)).execute(any())
    }

    @Test
    fun sendBidRequest_GivenAdUnitInProcessAndNewOnes_ExecuteOnlyNewOnes() {
        val requestsAreScheduled = CountDownLatch(1)
        val requestsAreDone = CountDownLatch(2)
        val asyncExecutor = Executors.newFixedThreadPool(3)
        executor = Executor {
            asyncExecutor.execute {
                requestsAreScheduled.await()
                it.run()
                requestsAreDone.countDown()
            }
        }
        givenNewSender()

        val adUnit = createAdUnit()
        val otherAdUnit = createAdUnit()

        sender.sendBidRequest(adUnit, mock(), mock())
        sender.sendBidRequest(adUnit, mock(), mock())
        sender.sendBidRequest(otherAdUnit, mock(), mock())

        requestsAreScheduled.countDown()
        assertThat(requestsAreDone.await(1, TimeUnit.SECONDS)).isTrue()

        argumentCaptor<CacheAdUnit> {
            verify(cdbRequestFactory, times(2)).createRequest(capture(), any())

            assertThat(allValues).containsExactlyInAnyOrder(adUnit, otherAdUnit)
        }
    }

    @Test
    fun sendBidRequest_GivenExceptionDuringCall_TaskIsCleaned() {
        whenever(api.loadCdb(any(), any())).doThrow(RuntimeException::class)

        sender.sendBidRequest(createAdUnit(), mock(), mock())

        assertThat(sender.pendingTaskAdUnits).isEmpty()
    }

    @Test
    fun sendBidRequest_GivenRejectedAsyncExecution_TaskIsCleaned() {
        executor = Executor { throw RejectedExecutionException() }
        givenNewSender()

        assertThatCode {
            sender.sendBidRequest(createAdUnit(), mock(), mock())
        }.isInstanceOf(RuntimeException::class.java)

        assertThat(sender.pendingTaskAdUnits).isEmpty()
    }

    @Test
    fun cancelAllPendingTasks_GivenNoTask_DoNothing() {
        assertThatCode {
            sender.cancelAllPendingTasks()
        }.doesNotThrowAnyException()
    }

    @Test
    fun cancelAllPendingTasks_GivenSomeTasks_InterruptThem() {
        executor = Executors.newFixedThreadPool(2)
        givenNewSender()

        val bothCallsAreWaiting = CountDownLatch(2)
        val waitingLatch = CountDownLatch(1)
        val bothCallsAreInterrupted = CountDownLatch(2)

        doAnswer {
            bothCallsAreWaiting.countDown()
            try {
                waitingLatch.await()
            } catch (e: InterruptedException) {
                bothCallsAreInterrupted.countDown()
                throw e
            }
            null
        }.whenever(api).loadCdb(anyOrNull(), anyOrNull())

        sender.sendBidRequest(createAdUnit(), mock(), mock())
        sender.sendBidRequest(createAdUnit(), mock(), mock())
        assertThat(bothCallsAreWaiting.await(1, TimeUnit.SECONDS)).isTrue()

        sender.cancelAllPendingTasks()

        assertThat(bothCallsAreInterrupted.await(1, TimeUnit.SECONDS)).isTrue()
    }

    private fun createAdUnit(): CacheAdUnit {
        val id = "id" + adUnitId.incrementAndGet()
        return CacheAdUnit(AdSize(1, 2), id, CRITEO_BANNER)
    }
}
