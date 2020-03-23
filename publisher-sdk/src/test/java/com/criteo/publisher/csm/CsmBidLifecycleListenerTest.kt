package com.criteo.publisher.csm

import com.criteo.publisher.Clock
import com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.model.*
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
  private lateinit var sendingQueueProducer: MetricSendingQueueProducer

  @Mock
  private lateinit var clock: Clock

  private lateinit var listener: CsmBidLifecycleListener

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    listener = CsmBidLifecycleListener(
        repository,
        sendingQueueProducer,
        clock
    )
  }

  @Test
  fun onSdkInitialized_PushAllMetricsInQueue() {
    listener.onSdkInitialized()

    verify(sendingQueueProducer).pushAllInQueue(repository)
  }

  @Test
  fun onCdbCallStarted_GivenMultipleSlots_UpdateAllStartTimeOfMetricsById() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    clock.stub {
      on { currentTimeInMillis } doReturn 42
    }

    listener.onCdbCallStarted(request)


    assertRepositoryIsUpdatedByIds("id1", "id2") {
      verify(it).setCdbCallStartTimestamp(42)
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
      verify(it).setCdbCallEndTimestamp(1337)
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
      verify(it).setCdbCallTimeoutTimestamp(1337)
      verify(it).setReadyToSend(true)
    }

    verify(sendingQueueProducer).pushAllReadyToSendInQueue(repository)
  }

  @Test
  fun onBidConsumed_GivenNotExpiredBid_SetElapsedTimeAndReadyToSend() {
    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)

    val slot = mock<Slot>() {
      on { impressionId } doReturn "id"
      on { isExpired(clock) } doReturn false
    }

    clock.stub {
      on { currentTimeInMillis } doReturn 42
    }

    listener.onBidConsumed(adUnit, slot)

    assertRepositoryIsUpdatedById("id") {
      verify(it).setElapsedTimestamp(42)
      verify(it).setReadyToSend(true)
    }

    verify(sendingQueueProducer).pushAllReadyToSendInQueue(repository)
  }

  @Test
  fun onBidConsumed_GivenExpiredBid_SetReadyToSend() {
    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)

    val slot = mock<Slot>() {
      on { impressionId } doReturn "id"
      on { isExpired(clock) } doReturn true
    }

    listener.onBidConsumed(adUnit, slot)

    assertRepositoryIsUpdatedById("id") {
      verify(it).setReadyToSend(true)
    }

    verify(sendingQueueProducer).pushAllReadyToSendInQueue(repository)
  }

  @Test
  fun onBidConsumed_GivenBidWithoutImpressionId_DoNothing() {
    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)

    val slot = mock<Slot>() {
      on { impressionId } doReturn null
    }

    listener.onBidConsumed(adUnit, slot)

    verifyZeroInteractions(repository)
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
      verifier: (Metric.Builder) -> Unit
  ) {
    argumentCaptor<String> {
      verify(repository, times(impressionIds.size)).updateById(capture(), verifier.asArgChecker())

      assertThat(allValues).containsExactlyInAnyOrder(*impressionIds)
    }
  }

  private fun assertRepositoryIsUpdatedById(
      impressionId: String,
      verifier: (Metric.Builder) -> Unit
  ) {
    verify(repository).updateById(eq(impressionId), verifier.asArgChecker())
  }

  private fun ((Metric.Builder) -> Unit).asArgChecker(): MetricRepository.MetricUpdater {
    return check {
      val metricBuilder: Metric.Builder = mock()

      it.update(metricBuilder)

      this(metricBuilder)
    }
  }

}