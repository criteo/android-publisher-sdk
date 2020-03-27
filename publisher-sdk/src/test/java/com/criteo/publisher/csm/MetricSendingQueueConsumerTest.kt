package com.criteo.publisher.csm

import com.criteo.publisher.Util.BuildConfigWrapper
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.network.PubSdkApi
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.util.concurrent.Executor

class MetricSendingQueueConsumerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Mock
  private lateinit var queue: MetricSendingQueue

  @Mock
  private lateinit var api: PubSdkApi

  @Mock
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Mock
  private lateinit var executor: Executor

  private lateinit var consumer: MetricSendingQueueConsumer

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1"
      on { isDebug } doReturn false
    }

    mockedDependenciesRule.dependencyProvider.stub {
      on { provideBuildConfigWrapper() } doReturn buildConfigWrapper
    }

    givenExecutor(Executor { it.run() })

    consumer = MetricSendingQueueConsumer(
        queue,
        api,
        buildConfigWrapper,
        executor
    )
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
    var isInExecutor = false
    givenExecutor(Executor {
      isInExecutor = true
      it.run()
      isInExecutor = false
    })

    doAnswer {
      assertThat(isInExecutor).isTrue()
    }.whenever(api).postCsm(any())

    consumer.sendMetricBatch()

    verify(api).postCsm(any())
  }

  private fun givenExecutor(executor: Executor) {
    doAnswer {
      val command = it.arguments[0] as Runnable
      executor.execute(command)
    }.whenever(this.executor).execute(any())
  }

}