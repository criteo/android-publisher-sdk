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
  fun onCdbCallFinished_GivenOnlyNoBid_PushReadyToSendInQueue() {
    val request = givenCdbRequestWithSlots("id")

    val response = mock<CdbResponse>() {
      on { getSlotByImpressionId(any()) } doReturn null
    }

    listener.onCdbCallFinished(request, response)

    verify(sendingQueueProducer).pushInQueue(repository, "id")
  }

  @Test
  fun onCdbCallFinished_GivenOnlyInvalidBid_PushReadyToSendInQueue() {
    val request = givenCdbRequestWithSlots("id")

    val invalidSlot = mock<Slot>() {
      on { isValid } doReturn false
    }

    val response = mock<CdbResponse>() {
      on { getSlotByImpressionId(any()) } doReturn invalidSlot
    }

    listener.onCdbCallFinished(request, response)

    verify(sendingQueueProducer).pushInQueue(repository, "id")
  }

  @Test
  fun onCdbCallFinished_GivenOnlyValidBid_DoNotPushReadyToSendInQueue() {
    val request = givenCdbRequestWithSlots("id")

    val validSlot = mock<Slot>() {
      on { isValid } doReturn true
    }

    val response = mock<CdbResponse>() {
      on { getSlotByImpressionId(any()) } doReturn validSlot
    }

    listener.onCdbCallFinished(request, response)

    verify(sendingQueueProducer, never()).pushInQueue(any(), any())
  }

  @Test
  fun onCdbCallFinished_GivenNoBidAndInvalidBidAndValidBidReceived_UpdateThemByIdAccordingly() {
    val request = givenCdbRequestWithSlots("noBidId", "invalidId", "validId")

    val invalidSlot = mock<Slot>() {
      on { isValid } doReturn false
    }

    val validSlot = mock<Slot>() {
      on { isValid } doReturn true
    }

    val response = mock<CdbResponse>() {
      on { getSlotByImpressionId("noBidId") } doReturn null
      on { getSlotByImpressionId("invalidId") } doReturn invalidSlot
      on { getSlotByImpressionId("validId") } doReturn validSlot
    }

    clock.stub {
      on { currentTimeInMillis } doReturn 1337
    }

    listener.onCdbCallFinished(request, response)

    assertNoBidSlotIsReceived("noBidId")
    assertInvalidBidSlotIsReceived("invalidId")
    assertValidBidSlotIsReceived("validId")

    verify(sendingQueueProducer).pushInQueue(repository, "noBidId")
    verify(sendingQueueProducer).pushInQueue(repository, "invalidId")
    verifyNoMoreInteractions(sendingQueueProducer)
  }

  @Test
  fun onCdbCallFailed_GivenNotATimeoutException_UpdateAllForNetworkError() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    listener.onCdbCallFailed(request, mock<IOException>())

    assertNetworkErrorIsReceived("id1")
    assertNetworkErrorIsReceived("id2")
  }

  @Test
  fun onCdbCallFailed_GivenTimeoutExceptionAndMultipleRequestSlots_UpdateAllByIdForTimeout() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    clock.stub {
      on { currentTimeInMillis } doReturn 1337
    }

    listener.onCdbCallFailed(request, mock<SocketTimeoutException>())

    assertTimeoutErrorIsReceived("id1")
    assertTimeoutErrorIsReceived("id2")

    verify(sendingQueueProducer).pushInQueue(repository, "id1")
    verify(sendingQueueProducer).pushInQueue(repository, "id2")
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

    verify(sendingQueueProducer).pushInQueue(repository, "id")
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

    verify(sendingQueueProducer).pushInQueue(repository, "id")
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
      verify(repository, times(impressionIds.size)).addOrUpdateById(capture(), verifier.asArgChecker())

      assertThat(allValues).containsExactlyInAnyOrder(*impressionIds)
    }
  }

  private fun assertRepositoryIsUpdatedById(
      impressionId: String,
      verifier: (Metric.Builder) -> Unit
  ) {
    verify(repository).addOrUpdateById(eq(impressionId), verifier.asArgChecker())
  }

  private fun assertValidBidSlotIsReceived(impressionId: String) {
    assertRepositoryIsUpdatedById(impressionId) {
      verify(it).setCdbCallEndTimestamp(clock.currentTimeInMillis)
      verify(it).setCachedBidUsed(true);
      verifyNoMoreInteractions(it)
    }
  }

  private fun assertTimeoutErrorIsReceived(impressionId: String) {
    assertRepositoryIsUpdatedById(impressionId) {
      verify(it).setCdbCallTimeout(true)
      verify(it).setReadyToSend(true)
      verifyNoMoreInteractions(it)
    }
  }

  private fun assertNetworkErrorIsReceived(impressionId: String) =
      assertInvalidBidSlotIsReceived(impressionId)

  private fun assertInvalidBidSlotIsReceived(impressionId: String) {
    assertRepositoryIsUpdatedById(impressionId) {
      verify(it).setReadyToSend(true)
      verifyNoMoreInteractions(it)
    }
  }

  private fun assertNoBidSlotIsReceived(impressionId: String) {
    assertRepositoryIsUpdatedById(impressionId) {
      verify(it).setCdbCallEndTimestamp(clock.currentTimeInMillis)
      verify(it).setReadyToSend(true)
      verifyNoMoreInteractions(it)
    }
  }

  private fun ((Metric.Builder) -> Unit).asArgChecker(): MetricRepository.MetricUpdater {
    return check {
      val metricBuilder: Metric.Builder = mock()

      it.update(metricBuilder)

      this(metricBuilder)
    }
  }

}