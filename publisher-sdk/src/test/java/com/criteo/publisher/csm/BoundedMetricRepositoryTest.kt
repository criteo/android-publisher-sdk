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

import com.criteo.publisher.csm.MetricRepository.MetricUpdater
import com.criteo.publisher.util.BuildConfigWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class BoundedMetricRepositoryTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Mock
  private lateinit var delegate: MetricRepository

  @InjectMocks
  private lateinit var repository: BoundedMetricRepository

  @Test
  fun getAllStoredMetrics_GivenDelegate_DelegateToIt() {
    val metric = mock<Metric>()

    delegate.stub {
      on { allStoredMetrics } doReturn listOf(metric)
    }

    val metrics = repository.allStoredMetrics

    assertThat(metrics).containsExactly(metric)
  }

  @Test
  fun moveById_GivenDelegate_DelegateToIt() {
    val mover = mock<MetricMover>()

    repository.moveById("id", mover)

    verify(delegate).moveById("id", mover)
  }

  @Test
  fun getTotalSize_GivenDelegate_DelegateToIt() {
    delegate.stub {
      on { getTotalSize() } doReturn 42
    }

    val size = repository.getTotalSize()

    assertThat(size).isEqualTo(42)
  }

  @Test
  fun contains_GivenDelegate_DelegateToIt() {
    delegate.stub {
      on { contains("id") } doReturn true
    }

    val contained = repository.contains("id")

    assertThat(contained).isTrue()
    verify(delegate).contains("id")
  }

  @Test
  fun updateById_GivenDelegateWithSizeBelowThreshold_DelegateToIt() {
    val updater = mock<MetricUpdater>()

    delegate.stub {
      on { getTotalSize() } doReturn 5
    }

    buildConfigWrapper.stub {
      on { maxSizeOfCsmMetricsFolder } doReturn 6
    }

    repository.addOrUpdateById("id", updater)

    verify(delegate).addOrUpdateById("id", updater)
  }

  @Test
  fun updateById_GivenDelegateWithSizeAboveThresholdAndExistingMetric_DelegateToIt() {
    val updater = mock<MetricUpdater>()

    delegate.stub {
      on { getTotalSize() } doReturn 42
      on { contains("id") } doReturn true
    }

    buildConfigWrapper.stub {
      on { maxSizeOfCsmMetricsFolder } doReturn 6
    }

    repository.addOrUpdateById("id", updater)

    verify(delegate).addOrUpdateById("id", updater)
  }

  @Test
  fun updateById_GivenDelegateWithSizeAboveThresholdAndNotExistingMetric_StopInvocation() {
    val updater = mock<MetricUpdater>()

    delegate.stub {
      on { totalSize } doReturn 42
      on { contains("id") } doReturn false
    }

    buildConfigWrapper.stub {
      on { maxSizeOfCsmMetricsFolder } doReturn 6
    }

    repository.addOrUpdateById("id", updater)

    verify(delegate, never()).addOrUpdateById("id", updater)
  }

}