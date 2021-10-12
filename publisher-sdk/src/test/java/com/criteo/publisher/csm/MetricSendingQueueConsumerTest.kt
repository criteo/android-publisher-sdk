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

package com.criteo.publisher.csm

import com.criteo.publisher.concurrent.DirectMockExecutor
import com.criteo.publisher.integration.Integration.FALLBACK
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.Config
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.BuildConfigWrapper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

class MetricSendingQueueConsumerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var queue: MetricSendingQueue

  @Mock
  private lateinit var api: PubSdkApi

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @SpyBean
  private lateinit var config: Config

  private val executor = DirectMockExecutor()

  private lateinit var consumer: MetricSendingQueueConsumer

  @Before
  fun setUp() {
    buildConfigWrapper.stub {
      on { preconditionThrowsOnException() } doReturn false
    }

    consumer = MetricSendingQueueConsumer(
        queue,
        api,
        buildConfigWrapper,
        config,
        executor
    )
  }

  @Test
  fun sendMetricBatch_GivenDeactivatedFeature_DoNothing() {
    config.stub {
      on { isCsmEnabled } doReturn false
    }

    consumer.sendMetricBatch()

    verifyZeroInteractions(queue)
    verifyZeroInteractions(api)
  }

  @Test
  fun sendMetricBatch_GivenFeatureDeactivatedAfterAPreviousSending_DoNothing() {
    sendMetricBatch_GivenSomeMetricsInBatch_SendThemAsyncWithApi()
    clearInvocations(queue, api)

    config.stub {
      on { isCsmEnabled } doReturn false
    }

    consumer.sendMetricBatch()

    verifyZeroInteractions(queue)
    verifyZeroInteractions(api)
  }

  @Test
  fun sendMetricBatch_GivenSomeMetricsAndIOException_RollbackMetrics() {
    val metric1 = Metric.builder("id1").build()
    val metric2 = Metric.builder("id2").build()

    queue.stub {
      on { poll(any()) } doReturn listOf(metric1, metric2)
    }

    api.stub {
      on { postCsm(any()) } doThrow IOException::class
    }

    consumer.sendMetricBatch()

    verify(queue).offer(metric1)
    verify(queue).offer(metric2)
  }

  @Test
  fun sendMetricBatch_GivenNoMetricsInBatch_DoNotSendAnything() {
    buildConfigWrapper.stub {
      on { csmBatchSize } doReturn 42
    }

    queue.stub {
      on { poll(42) } doReturn listOf()
    }

    consumer.sendMetricBatch()

    verify(api, never()).postCsm(any())
  }

  @Test
  fun sendMetricBatch_GivenSomeMetricsInBatch_SendThemAsyncWithApi() {
    val metric1 = Metric.builder("id1").setProfileId(1337).build()
    val metric2 = Metric.builder("id2").setProfileId(1337).build()

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
      on { csmBatchSize } doReturn 42
    }

    queue.stub {
      on { poll(42) } doReturn listOf(metric1, metric2)
    }

    val expectedRequest = MetricRequest.create(
        listOf(metric1, metric2),
        "1.2.3",
        1337
    )

    consumer.sendMetricBatch()

    verify(api).postCsm(expectedRequest)
  }

  @Test
  fun sendMetricBatch_GivenSomeMetricsWithDifferentProfileId_SendThemGroupedByProfileId() {
    val metric1 = Metric.builder("id1").setProfileId(1337).build()
    val metric2 = Metric.builder("id2").build()
    val metric3 = Metric.builder("id3").setProfileId(1337).build()
    val metric4 = Metric.builder("id4").build()

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
      on { csmBatchSize } doReturn 42
    }

    queue.stub {
      on { poll(42) } doReturn listOf(metric1, metric2, metric3, metric4)
    }

    doNothing().doThrow(IOException::class.java).whenever(api).postCsm(any())

    consumer.sendMetricBatch()

    verify(queue, never()).offer(metric1)
    verify(queue).offer(metric2)
    verify(queue, never()).offer(metric3)
    verify(queue).offer(metric4)
  }

  @Test
  fun sendMetricBatch_GivenOneProfileIdSentButExceptionAfter_RollbackOnlyRemainingMetrics() {
    val metric1 = Metric.builder("id1").build()
    val metric2 = Metric.builder("id2").setProfileId(1337).build()
    val metric3 = Metric.builder("id3").setProfileId(FALLBACK.profileId).build()
    val metric4 = Metric.builder("id4").setProfileId(1337).build()

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
      on { csmBatchSize } doReturn 42
    }

    queue.stub {
      on { poll(42) } doReturn listOf(metric1, metric2, metric3, metric4)
    }

    val expectedRequest1 = MetricRequest.create(
        listOf(metric1, metric3),
        "1.2.3",
        FALLBACK.profileId
    )

    val expectedRequest2 = MetricRequest.create(
        listOf(metric2, metric4),
        "1.2.3",
        1337
    )

    consumer.sendMetricBatch()

    verify(api).postCsm(expectedRequest1)
    verify(api).postCsm(expectedRequest2)
    verifyNoMoreInteractions(api)
  }

  @Test
  fun sendMetricBatch_GivenExecutor_CallApiInExecutor() {
    queue.stub {
      on { poll(any()) } doReturn listOf(Metric.builder("id1").build())
    }

    doAnswer {
      executor.expectIsRunningInExecutor()
    }.whenever(api).postCsm(any())

    consumer.sendMetricBatch()

    verify(api).postCsm(any())
    executor.verifyExpectations()
  }

}