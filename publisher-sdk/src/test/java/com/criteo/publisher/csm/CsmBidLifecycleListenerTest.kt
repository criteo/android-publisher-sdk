package com.criteo.publisher.csm

import com.criteo.publisher.Clock
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbRequestSlot
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CsmBidLifecycleListenerTest {

  @Mock
  private lateinit var repository: MetricRepository

  @Mock
  private lateinit var clock: Clock

  private lateinit var listener: CsmBidLifecycleListener

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    listener = CsmBidLifecycleListener(
        repository,
        clock
    )
  }

  @Test
  fun onCdbCallStarted_GivenMultipleSlots_UpdateAllStartTimeOfMetricsById() {
    val slot1: CdbRequestSlot = mock() {
      on { impressionId } doReturn "id1"
    }

    val slot2: CdbRequestSlot = mock() {
      on { impressionId } doReturn "id2"
    }

    val request: CdbRequest = mock() {
      on { slots } doReturn listOf(slot1, slot2)
    }

    clock.stub {
      on { currentTimeInMillis } doReturn 42
    }

    listener.onCdbCallStarted(request)

    argumentCaptor<String> {
      verify(repository, times(2)).updateById(capture(), check {
        val metricBuilder: MetricBuilder = mock()

        it.update(metricBuilder)

        verify(metricBuilder).setCdbCallStartAbsolute(42)
      })

      assertThat(allValues).containsExactlyInAnyOrder("id1", "id2")
    }
  }

}