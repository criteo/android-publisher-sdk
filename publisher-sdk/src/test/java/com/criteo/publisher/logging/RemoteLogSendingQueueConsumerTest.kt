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

import com.criteo.publisher.concurrent.DirectMockExecutor
import com.criteo.publisher.csm.ConcurrentSendingQueue
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

class RemoteLogSendingQueueConsumerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Mock
  private lateinit var queue: ConcurrentSendingQueue<RemoteLogRecords>

  @Mock
  private lateinit var api: PubSdkApi

  @Mock
  private lateinit var advertisingInfo: AdvertisingInfo

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  private val executor = DirectMockExecutor()

  private lateinit var consumer: RemoteLogSendingQueueConsumer

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    doReturn(false).whenever(buildConfigWrapper).preconditionThrowsOnException()

    consumer = RemoteLogSendingQueueConsumer(
        queue,
        api,
        buildConfigWrapper,
        advertisingInfo,
        executor
    )
  }

  @Test
  fun sendRemoteLogBatch_GivenSomeLogsAndIOException_RollbackLogs() {
    val logs1 = mock<RemoteLogRecords>()
    val logs2 = mock<RemoteLogRecords>()

    whenever(queue.poll(any())).doReturn(listOf(logs1, logs2))
    doThrow(IOException::class).whenever(api).postLogs(any())

    consumer.sendRemoteLogBatch()

    verify(queue).offer(logs1)
    verify(queue).offer(logs2)
  }

  @Test
  fun sendRemoteLogBatch_GivenNoLogsInBatch_DoNotSendAnything() {
    doReturn(42).whenever(buildConfigWrapper).remoteLogBatchSize
    whenever(queue.poll(any())).doReturn(listOf())

    consumer.sendRemoteLogBatch()

    verify(api, never()).postLogs(any())
  }

  @Test
  fun sendRemoteLogBatch_GivenSomeLogsInBatch_SendThemAsyncWithApi() {
    val logs1 = mock<RemoteLogRecords>()
    val logs2 = mock<RemoteLogRecords>()

    doReturn(42).whenever(buildConfigWrapper).remoteLogBatchSize
    whenever(queue.poll(any())).doReturn(listOf(logs1, logs2))

    consumer.sendRemoteLogBatch()

    verify(api).postLogs(listOf(logs1, logs2))
  }

  @Test
  fun sendRemoteLogBatch_GivenExecutor_CallApiInExecutor() {
    val logs = mock<RemoteLogRecords>()
    whenever(queue.poll(any())).doReturn(listOf(logs))

    doAnswer {
      executor.expectIsRunningInExecutor()
    }.whenever(api).postLogs(any())

    consumer.sendRemoteLogBatch()

    verify(api).postLogs(any())
    executor.verifyExpectations()
  }

  @Test
  fun injectMissingDeviceId_GivenRemoteLogWithoutDeviceId_InjectIt() {
    val contextWithoutDeviceId = mock<RemoteLogRecords.RemoteLogContext>() {
      on { deviceId } doReturn null
    }
    val contextWithDeviceId = mock<RemoteLogRecords.RemoteLogContext>() {
      on { deviceId } doReturn "device-id"
    }

    val logsWithoutDeviceId = mock<RemoteLogRecords>() {
      on { context } doReturn contextWithoutDeviceId
    }
    val logsWithDeviceId = mock<RemoteLogRecords>() {
      on { context } doReturn contextWithDeviceId
    }

    whenever(advertisingInfo.advertisingId).doReturn("new-device-id")
    whenever(queue.poll(any())).doReturn(listOf(logsWithoutDeviceId, logsWithDeviceId))

    doAnswer {
      executor.expectIsRunningInExecutor()
    }.whenever(api).postLogs(any())

    consumer.sendRemoteLogBatch()

    verify(contextWithDeviceId, never()).deviceId = any()
    verify(contextWithoutDeviceId).deviceId = "new-device-id"
    verify(api).postLogs(any())
    executor.verifyExpectations()
  }
}
