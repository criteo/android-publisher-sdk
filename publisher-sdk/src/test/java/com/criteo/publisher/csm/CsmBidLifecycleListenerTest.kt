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

import com.criteo.publisher.Clock
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.CacheAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbRequestSlot
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.model.Config
import com.criteo.publisher.privacy.ConsentData
import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.Executor

class CsmBidLifecycleListenerTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var repository: MetricRepository

  @Mock
  private lateinit var sendingQueueProducer: MetricSendingQueueProducer

  @Mock
  private lateinit var clock: Clock

  @Mock
  private lateinit var config: Config

  @Mock
  private lateinit var consentData: ConsentData

  private lateinit var executor: Executor

  private lateinit var listener: CsmBidLifecycleListener

  @Before
  fun setUp() {
    executor = Executor { it.run() }

    config.stub {
      on { isCsmEnabled } doReturn true
    }

    whenever(consentData.isConsentGiven()).thenReturn(true)

    listener = CsmBidLifecycleListener(
        repository,
        sendingQueueProducer,
        clock,
        config,
        consentData,
        executor
    )
  }

  @Test
  fun onSdkInitialized_PushAllMetricsInQueue() {
    listener.onSdkInitialized()

    verify(sendingQueueProducer).pushAllInQueue(repository)
  }

  @Test
  fun onSdkInitialized_GivenDeactivatedFeature_DoNothing() {
    givenDeactivatedFeature()

    listener.onSdkInitialized()

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onSdkInitialized_GivenConsentNotGiven_DoNothing() {
    givenConsentNotGiven()

    listener.onSdkInitialized()

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onCdbCallStarted_GivenDeactivatedFeature_DoNothing() {
    givenDeactivatedFeature()

    listener.onCdbCallStarted(mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onCdbCallStarted_GivenConsentNotGiven_DoNothing() {
    givenConsentNotGiven()

    listener.onCdbCallStarted(mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onCdbCallStarted_GivenMultipleSlots_UpdateAllStartTimeAndRequestIdOfMetricsById() {
    val request = givenCdbRequestWithSlots("id1", "id2").stub {
      on { profileId } doReturn 1337
      on { id } doReturn "myRequestId"
    }

    clock.stub {
      on { currentTimeInMillis } doReturn 42
    }

    listener.onCdbCallStarted(request)

    assertRepositoryIsUpdatedByIds("id1", "id2") {
      verify(it).setCdbCallStartTimestamp(42)
      verify(it).setRequestGroupId("myRequestId")
      verify(it).setProfileId(1337)
    }
  }

  @Test
  fun onCdbCallFinished_GivenDeactivatedFeature_DoNothing() {
    givenDeactivatedFeature()

    listener.onCdbCallFinished(mock(), mock())

    verifyFeatureIsDeactivated()
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

    val invalidSlot = mock<CdbResponseSlot>() {
      on { isValid() } doReturn false
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

    val validSlot = mock<CdbResponseSlot>() {
      on { isValid() } doReturn true
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

    val invalidSlot = mock<CdbResponseSlot>() {
      on { isValid() } doReturn false
    }

    val validSlot = mock<CdbResponseSlot>() {
      on { isValid() } doReturn true
      on { zoneId } doReturn 42
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
    assertValidBidSlotIsReceived("validId", 42)

    verify(sendingQueueProducer).pushInQueue(repository, "noBidId")
    verify(sendingQueueProducer).pushInQueue(repository, "invalidId")
    verifyNoMoreInteractions(sendingQueueProducer)
  }

  @Test
  fun onCdbCallFailed_GivenDeactivatedFeature_DoNothing() {
    givenDeactivatedFeature()

    listener.onCdbCallFailed(mock(), mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onCdbCallFailed_GivenConsentNotGiven_DoNothing() {
    givenConsentNotGiven()

    listener.onCdbCallFailed(mock(), mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onCdbCallFailed_GivenNotATimeoutException_UpdateAllForNetworkError() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    listener.onCdbCallFailed(request, mock<IOException>())

    assertNetworkErrorIsReceived("id1")
    assertNetworkErrorIsReceived("id2")

    verify(sendingQueueProducer).pushInQueue(repository, "id1")
    verify(sendingQueueProducer).pushInQueue(repository, "id2")
  }

  @Test
  fun onCdbCallFailed_GivenSocketTimeoutExceptionAndMultipleRequestSlots_UpdateAllByIdForTimeout() {
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
  fun onCdbCallFailed_GivenInterruptedIOExceptionAndMultipleRequestSlots_UpdateAllByIdForTimeout() {
    val request = givenCdbRequestWithSlots("id1", "id2")

    clock.stub {
      on { currentTimeInMillis } doReturn 1337
    }

    listener.onCdbCallFailed(request, mock<InterruptedIOException>())

    assertTimeoutErrorIsReceived("id1")
    assertTimeoutErrorIsReceived("id2")

    verify(sendingQueueProducer).pushInQueue(repository, "id1")
    verify(sendingQueueProducer).pushInQueue(repository, "id2")
  }

  @Test
  fun onBidConsumed_GivenDeactivatedFeature_DoNothing() {
    givenDeactivatedFeature()
    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)

    listener.onBidConsumed(adUnit, mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onBidConsumed_GivenConsentNotGiven_DoNothing() {
    givenConsentNotGiven()

    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)

    listener.onBidConsumed(adUnit, mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onBidConsumed_GivenNotExpiredBid_SetElapsedTimeAndReadyToSend() {
    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)

    val slot = mock<CdbResponseSlot>() {
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

    val slot = mock<CdbResponseSlot>() {
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

    val slot = mock<CdbResponseSlot>() {
      on { impressionId } doReturn null
    }

    listener.onBidConsumed(adUnit, slot)

    verifyZeroInteractions(repository)
  }

  @Test
  fun onBidsCached_GivenDeactivatedFeature_DoNothing() {
    givenDeactivatedFeature()

    listener.onBidCached(mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onBidsCached_GivenConsentNotGiven_DoNothing() {
    givenConsentNotGiven()

    listener.onBidCached(mock())

    verifyFeatureIsDeactivated()
  }

  @Test
  fun onBidsCached_GivenValidSlots_SetCachedBidUsed() {
    val validSlot = mock<CdbResponseSlot>() {
      on { isValid() } doReturn true
      on { zoneId } doReturn 42
      on { impressionId } doReturn "id"
    }

    listener.onBidCached(validSlot)

    assertRepositoryIsUpdatedById("id") {
      verify(it).setCachedBidUsed(true)
      verifyNoMoreInteractions(it)
    }
  }

  @Test
  fun onBidsCached_GivenInValidSlots_DontSetBidCached() {
    val invalidSlot = mock<CdbResponseSlot>() {
      on { isValid() } doReturn false
      on { zoneId } doReturn 42
      on { impressionId } doReturn "id"
    }

    listener.onBidCached(invalidSlot)

    verify(repository, never()).addOrUpdateById(any(), any())
  }

  private fun givenCdbRequestWithSlots(vararg impressionIds: String): CdbRequest {
    val slots = impressionIds.map { impressionId ->
      mock<CdbRequestSlot> {
        on { this.impressionId } doReturn impressionId
      }
    }.toList()

    return mock {
      on { this.slots } doReturn slots
    }
  }

  private fun assertRepositoryIsUpdatedByIds(
      vararg impressionIds: String,
      verifier: (Metric.Builder) -> Unit
  ) {
    argumentCaptor<String> {
      verify(repository, times(impressionIds.size)).addOrUpdateById(
          capture(),
          verifier.asArgChecker()
      )

      assertThat(allValues).containsExactlyInAnyOrder(*impressionIds)
    }
  }

  private fun assertRepositoryIsUpdatedById(
      impressionId: String,
      verifier: (Metric.Builder) -> Unit
  ) {
    verify(repository).addOrUpdateById(eq(impressionId), verifier.asArgChecker())
  }

  private fun assertValidBidSlotIsReceived(impressionId: String, zoneId: Int) {
    assertRepositoryIsUpdatedById(impressionId) {
      verify(it).setCdbCallEndTimestamp(clock.currentTimeInMillis)
      verify(it).setZoneId(zoneId)
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

  private fun givenDeactivatedFeature() {
    config.stub {
      on { isCsmEnabled } doReturn false
    }
  }

  private fun givenConsentNotGiven() {
    whenever(consentData.isConsentGiven()).thenReturn(false)
  }

  private fun verifyFeatureIsDeactivated() {
    verifyZeroInteractions(repository)
    verifyZeroInteractions(clock)
    verifyZeroInteractions(sendingQueueProducer)
  }
}
