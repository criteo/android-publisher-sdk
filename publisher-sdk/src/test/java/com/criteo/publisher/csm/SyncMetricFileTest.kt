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

import android.util.AtomicFile
import com.criteo.publisher.util.JsonSerializer
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

class SyncMetricFileTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var atomicFile: AtomicFile

  @Mock
  private lateinit var jsonSerializer: JsonSerializer

  private lateinit var metricFile: SyncMetricFile

  @Before
  fun setUp() {
    metricFile = spy(SyncMetricFile("id", atomicFile, jsonSerializer))
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
