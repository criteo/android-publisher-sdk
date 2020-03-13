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
    val request = givenCdbRequestWithSlots("id1", "id2")

    clock.stub {
      on { currentTimeInMillis } doReturn 42
    }

    listener.onCdbCallStarted(request)

    assertRepositoryIsUpdatedByIds("id1", "id2") {
      verify(it).setCdbCallStartAbsolute(42)
    }
  }

  @Test
  fun onCdbCallFinished_GivenMultipleRequestSlots_UpdateAllEndTimeOfMetricsById() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    clock.stub {
      on { currentTimeInMillis } doReturn 1337
    }

    listener.onCdbCallFinished(request, mock())

    assertRepositoryIsUpdatedByIds("id1", "id2") {
      verify(it).setCdbCallEndAbsolute(1337)
    }
  }

  private fun givenCdbRequestWithSlots(vararg impressionIds: String): CdbRequest {
    val slots = impressionIds.map { impressionId ->
      mock<CdbRequestSlot>() {
        on { getImpressionId() } doReturn impressionId
      }
    }.toList()

    return mock() {
      on { getSlots() } doReturn slots
    }
  }

  private fun assertRepositoryIsUpdatedByIds(
      vararg impressionIds: String,
      verifier: (MetricBuilder) -> Unit
  ) {
    argumentCaptor<String> {
      verify(repository, times(impressionIds.size)).updateById(capture(), check {
        val metricBuilder: MetricBuilder = mock()

        it.update(metricBuilder)

        verifier(metricBuilder)
      })

      assertThat(allValues).containsExactlyInAnyOrder(*impressionIds)
    }
  }

}