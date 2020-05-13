package com.criteo.publisher.csm

import com.criteo.publisher.concurrent.DirectMockExecutor
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.Config
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException

class MetricSendingQueueConsumerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

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
    MockitoAnnotations.initMocks(this)

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
    val metric1 = Metric.builder("id1").build()
    val metric2 = Metric.builder("id2").build()

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
      on { profileId } doReturn 1337
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