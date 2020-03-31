package com.criteo.publisher.csm

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock

class MetricSendingQueueProducerTest {

  @Mock
  private lateinit var repository: MetricRepository

  @Mock
  private lateinit var queue: MetricSendingQueue

  private lateinit var producer: MetricSendingQueueProducer

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    producer = MetricSendingQueueProducer(queue)
  }

  @Test
  fun pushAllInQueue_GivenAnyMetric_PushAndMoveThem() {
    val metric1 = Metric.builder("id1")
        .setReadyToSend(false)
        .build()

    val metric2 = Metric.builder("id2")
        .setReadyToSend(true)
        .build()

    givenMetricInRepository(metric1, metric2)

    producer.pushAllInQueue(repository)

    assertOnlyThoseMetricsAreMoved(metric1, metric2)
  }

  @Test
  fun pushInQueue_GivenMetricId_MoveMetricMatchingId() {
    val shouldNotBeSent = Metric.builder("id1").build()
    val shouldBeSent = Metric.builder("id2").build()

    givenMetricInRepository(shouldNotBeSent, shouldBeSent)

    producer.pushInQueue(repository, "id2")

    assertOnlyThoseMetricsAreMoved(shouldBeSent)
  }

  private fun givenMetricInRepository(vararg metrics: Metric) {
    repository.stub {
      doAnswer { invocationOnMock: InvocationOnMock ->
        val impressionId: String = invocationOnMock.getArgument(0)
        val move: MetricMover = invocationOnMock.getArgument(1)

        metrics.firstOrNull { it.impressionId == impressionId }?.let {
          move.offerToDestination(it)
        }
      }.whenever(mock).moveById(any(), any())

      on { allStoredMetrics } doReturn metrics.asList()
    }
  }

  private fun assertOnlyThoseMetricsAreMoved(vararg movedMetrics: Metric) {
    val allMetrics = repository.allStoredMetrics
    val notMovedMetrics = allMetrics.minus(movedMetrics)

    movedMetrics.forEach {
      verify(repository).moveById(eq(it.impressionId), any())
      verify(queue).offer(it)
    }

    notMovedMetrics.forEach {
      verify(repository, never()).moveById(eq(it.impressionId), any())
      verify(queue, never()).offer(it)
    }
  }

}