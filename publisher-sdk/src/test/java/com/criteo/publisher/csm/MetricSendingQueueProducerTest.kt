package com.criteo.publisher.csm

import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
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

    verify(queue).offer(metric1)
    verify(queue).offer(metric2)
    assertOnlyThoseMetricsAreMoved(metric1, metric2)
  }

  @Test
  fun pushAllReadyToSendInQueue_GivenNoMetricReadyToSend_DoNothingAndKeepThem() {
    val shouldNotBeSent = Metric.builder("id")
        .setReadyToSend(false)
        .build()

    givenMetricInRepository(shouldNotBeSent)

    producer.pushAllReadyToSendInQueue(repository)

    verifyZeroInteractions(queue)
    assertOnlyThoseMetricsAreMoved()
  }

  @Test
  fun pushAllReadyToSendInQueue_GivenMetricReadyToSend_MoveOnlyThem() {
    val shouldNotBeSent = Metric.builder("id1")
        .setReadyToSend(false)
        .build()

    val shouldBeSent = Metric.builder("id2")
        .setReadyToSend(true)
        .build()

    givenMetricInRepository(shouldNotBeSent, shouldBeSent)

    producer.pushAllReadyToSendInQueue(repository)

    verify(queue).offer(shouldBeSent)
    verify(queue, never()).offer(shouldNotBeSent)
    assertOnlyThoseMetricsAreMoved(shouldBeSent)
  }

  private fun givenMetricInRepository(vararg metrics: Metric) {
    repository.stub {
      doAnswer { invocationOnMock: InvocationOnMock ->
        val move: MetricMover = invocationOnMock.getArgument(0)

        metrics.forEach {
          if (move.shouldMove(it)) {
            move.offerToDestination(it)
          }
        }
      }.whenever(mock).moveAllWith(any())

      on { allStoredMetrics } doReturn metrics.asList()
    }
  }

  private fun assertOnlyThoseMetricsAreMoved(vararg movedMetrics: Metric) {
    val allMetrics = repository.allStoredMetrics
    val notMovedMetrics = allMetrics.minus(movedMetrics)

    verify(repository).moveAllWith(check { move ->
      movedMetrics.forEach {
        assertThat(move.shouldMove(it)).isTrue()
      }

      notMovedMetrics.forEach {
        assertThat(move.shouldMove(it)).isFalse()
      }
    })
  }

}