package com.criteo.publisher.network

import com.criteo.publisher.concurrent.DirectMockExecutor
import com.criteo.publisher.model.*
import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.util.CdbCallListener
import com.criteo.publisher.util.CompletableFuture.completedFuture
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class BidRequestSenderTest {

    @Mock
    private lateinit var cdbRequestFactory: CdbRequestFactory

    @Mock
    private lateinit var remoteConfigRequestFactory: RemoteConfigRequestFactory

    @Mock
    private lateinit var api: PubSdkApi

    private var executor = Executor(Runnable::run)

    private lateinit var sender: BidRequestSender

    private val adUnitId = AtomicInteger(0)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(cdbRequestFactory.userAgent).doReturn(completedFuture(""))

        givenNewSender()
    }

    private fun givenNewSender(executor: Executor = this.executor) {
        sender = BidRequestSender(
                cdbRequestFactory,
                remoteConfigRequestFactory,
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
        givenNewSender(executor=executor)

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
        val adUnits = listOf(adUnit)
        val listener: CdbCallListener = mock()
        val request: CdbRequest = mock()
        val userAgent = "myUserAgent"
        val response: CdbResponse = mock()

        cdbRequestFactory.stub {
            on { createRequest(adUnits) } doReturn request
            on { it.userAgent } doReturn completedFuture(userAgent)
        }

        whenever(api.loadCdb(request, userAgent)).doReturn(response)

        sender.sendBidRequest(adUnits, listener)

        val inOrder = inOrder(listener)
        inOrder.verify(listener).onCdbRequest(request)
        inOrder.verify(listener).onCdbResponse(request, response)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun sendBidRequest_GivenAdUnitAndError_NotifyListener() {
        val adUnit = createAdUnit()
        val adUnits = listOf(adUnit)
        val listener: CdbCallListener = mock()
        val request: CdbRequest = mock()
        val userAgent = "myUserAgent"
        val exception = IOException()

        cdbRequestFactory.stub {
            on { createRequest(adUnits) } doReturn request
            on { it.userAgent } doReturn completedFuture(userAgent)
        }

        whenever(api.loadCdb(request, userAgent)).doThrow(exception)

        sender.sendBidRequest(adUnits, listener)

        val inOrder = inOrder(listener)
        inOrder.verify(listener).onCdbRequest(request)
        inOrder.verify(listener).onCdbError(request, exception)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun sendBidRequest_GivenExecutor_IsWorkingInExecutor() {
        val executor = DirectMockExecutor()
        givenNewSender(executor=executor)

        doAnswer {
            executor.expectIsRunningInExecutor()
            null
        }.whenever(api).loadCdb(anyOrNull(), anyOrNull())

        sender.sendBidRequest(listOf(createAdUnit()), mock())

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

        sender.sendBidRequest(listOf(createAdUnit()), mock())
        sender.sendBidRequest(listOf(createAdUnit()), mock())

        bothRequestsAreInParallel.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun sendBidRequest_GivenTwoSameAdUnitsInParallel_ExecuteOnlyTheFirstOne() {
        executor = spy(Executors.newFixedThreadPool(2))
        givenNewSender()

        whenever(api.loadCdb(any(), any())).doReturn(mock())

        val adUnit = createAdUnit()

        sender.sendBidRequest(listOf(adUnit), mock())
        sender.sendBidRequest(listOf(adUnit), mock())

        verify(executor, times(1)).execute(any())
    }

    @Test
    fun sendBidRequest_GivenAdUnitInProcessAndNewOnes_ExecuteOnlyNewOnes() {
        val requestsAreScheduled = CountDownLatch(1)
        val requestsAreDone = CountDownLatch(2)
        val asyncExecutor = Executors.newFixedThreadPool(2)
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

        sender.sendBidRequest(listOf(adUnit), mock())
        sender.sendBidRequest(listOf(adUnit, otherAdUnit), mock())

        requestsAreScheduled.countDown()
        assertThat(requestsAreDone.await(1, TimeUnit.SECONDS)).isTrue()

        argumentCaptor<List<CacheAdUnit>> {
            verify(cdbRequestFactory, times(2)).createRequest(capture())

            assertThat(allValues).containsExactlyInAnyOrder(listOf(adUnit), listOf(otherAdUnit))
        }
    }

    @Test
    fun sendBidRequest_GivenNoAdUnits_DoesNothing() {
        sender.sendBidRequest(emptyList(), mock())

        verifyZeroInteractions(api)
    }

    @Test
    fun sendBidRequest_GivenExceptionDuringCall_TaskIsCleaned() {
        whenever(api.loadCdb(any(), any())).doThrow(RuntimeException::class)

        sender.sendBidRequest(listOf(createAdUnit()), mock())

        assertThat(sender.pendingTaskAdUnits).isEmpty()
    }

    @Test
    fun sendBidRequest_GivenRejectedAsyncExecution_TaskIsCleaned() {
        executor = Executor { throw RejectedExecutionException() }
        givenNewSender()

        assertThatCode {
            sender.sendBidRequest(listOf(createAdUnit()), mock())
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
        val interruptedParties = AtomicInteger()

        doAnswer {
            bothCallsAreWaiting.countDown()
            try {
                waitingLatch.await()
            } catch (e: InterruptedException) {
                interruptedParties.incrementAndGet()
                throw e
            }
            null
        }.whenever(api).loadCdb(anyOrNull(), anyOrNull())

        sender.sendBidRequest(listOf(createAdUnit()), mock())
        sender.sendBidRequest(listOf(createAdUnit()), mock())
        assertThat(bothCallsAreWaiting.await(1, TimeUnit.SECONDS)).isTrue()

        sender.cancelAllPendingTasks()

        assertThat(interruptedParties).hasValue(2)
    }

    private fun createAdUnit(): CacheAdUnit {
        val id = "id" + adUnitId.incrementAndGet()
        return CacheAdUnit(AdSize(1, 2), id, CRITEO_BANNER)
    }
}