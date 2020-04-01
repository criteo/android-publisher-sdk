package com.criteo.publisher.csm

import com.criteo.publisher.Util.BuildConfigWrapper
import com.criteo.publisher.csm.MetricRepository.MetricUpdater
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

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