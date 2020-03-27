package com.criteo.publisher.csm

import android.support.v4.util.AtomicFile
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SyncMetricFileTest {

  @Mock
  private lateinit var atomicFile: AtomicFile

  @Mock
  private lateinit var parser: MetricParser

  private lateinit var metricFile: SyncMetricFile

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    metricFile = spy(SyncMetricFile("id", atomicFile, parser))
  }

  @Test
  fun moveWith_GivenSuccessfulMove_RemoveFromFileThenInjectToDestination() {
    val metric = Metric.builder("id").build()
    doReturn(metric).whenever(metricFile).read()

    val move = mock<MetricMover> {
      on { offerToDestination(metric) } doReturn true
    }

    metricFile.moveWith(move)

    val inOrder = inOrder(metricFile, move)
    inOrder.verify(metricFile).delete()
    inOrder.verify(move).offerToDestination(metric)
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun moveWith_GivenUnsuccessfulMove_RemoveFromFileThenInjectToDestinationThenRollback() {
    val metric = Metric.builder("id").build()
    doReturn(metric).whenever(metricFile).read()
    doNothing().whenever(metricFile).write(metric)

    val move = mock<MetricMover> {
      on { offerToDestination(metric) } doReturn false
    }

    metricFile.moveWith(move)

    val inOrder = inOrder(metricFile, move)
    inOrder.verify(metricFile).delete()
    inOrder.verify(move).offerToDestination(metric)
    inOrder.verify(metricFile).write(metric)
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun moveWith_GivenExceptionDuringMove_RemoveFromFileThenInjectToDestinationThenRollback() {
    val metric = Metric.builder("id").build()
    doReturn(metric).whenever(metricFile).read()
    doNothing().whenever(metricFile).write(metric)

    val exception = RuntimeException()

    val move = mock<MetricMover> {
      on { offerToDestination(metric) } doThrow exception
    }

    assertThatCode {
      metricFile.moveWith(move)
    }.isEqualTo(exception)

    val inOrder = inOrder(metricFile, move)
    inOrder.verify(metricFile).delete()
    inOrder.verify(move).offerToDestination(metric)
    inOrder.verify(metricFile).write(metric)
    inOrder.verifyNoMoreInteractions()
  }

}