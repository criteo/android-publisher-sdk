package com.criteo.publisher.csm

import com.criteo.publisher.Clock
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbRequestSlot
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.model.Slot
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.net.SocketTimeoutException

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
      verify(it, never()).setImpressionId(anyOrNull())
    }
  }

  @Test
  fun onCdbCallFinished_GivenRequestSlotsWithMatchingResponseSlot_UpdateImpressionIdOfValidOnes() {
    val request = givenCdbRequestWithSlots("id1", "id2", "id3")

    val invalidSlot = mock<Slot>() {
      on { isValid } doReturn false
    }

    val validSlot = mock<Slot>() {
      on { isValid } doReturn true
    }

    val response = mock<CdbResponse>() {
      on { getSlotByImpressionId("id1") } doReturn null
      on { getSlotByImpressionId("id2") } doReturn invalidSlot
      on { getSlotByImpressionId("id3") } doReturn validSlot
    }

    listener.onCdbCallFinished(request, response)

    assertRepositoryIsUpdatedById("id1") {
      verify(it, never()).setImpressionId(anyOrNull())
    }

    assertRepositoryIsUpdatedById("id2") {
      verify(it, never()).setImpressionId(anyOrNull())
    }

    assertRepositoryIsUpdatedById("id3") {
      verify(it).setImpressionId("id3")
    }
  }

  @Test
  fun onCdbCallFailed_GivenNotATimeoutException_DoNothing() {
    val request: CdbRequest = mock() {
      on { slots } doReturn listOf(mock(), mock())
    }

    listener.onCdbCallFailed(request, mock<IOException>())

    verifyZeroInteractions(repository)
  }

  @Test
  fun onCdbCallFailed_GivenTimeoutExceptionAndMultipleRequestSlots_UpdateAllEndTimeOfMetricsById() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    clock.stub {
      on { currentTimeInMillis } doReturn 1337
    }

    listener.onCdbCallFailed(request, mock<SocketTimeoutException>())

    assertRepositoryIsUpdatedByIds("id1", "id2") {
      verify(it).setCdbCallTimeoutAbsolute(1337)
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
      verify(repository, times(impressionIds.size)).updateById(capture(), verifier.asArgChecker())

      assertThat(allValues).containsExactlyInAnyOrder(*impressionIds)
    }
  }

  private fun assertRepositoryIsUpdatedById(
      impressionId: String,
      verifier: (MetricBuilder) -> Unit
  ) {
    verify(repository).updateById(eq(impressionId), verifier.asArgChecker())
  }

  private fun ((MetricBuilder) -> Unit).asArgChecker(): MetricRepository.MetricUpdater {
    return check {
      val metricBuilder: MetricBuilder = mock()

      it.update(metricBuilder)

      this(metricBuilder)
    }
  }

}