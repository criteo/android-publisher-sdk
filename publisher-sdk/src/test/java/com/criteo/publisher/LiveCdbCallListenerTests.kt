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

package com.criteo.publisher

import com.criteo.publisher.bid.BidLifecycleListener
import com.criteo.publisher.model.CacheAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.privacy.ConsentData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LiveCdbCallListenerTests {
  @Mock
  private lateinit var bidListener: BidListener

  @Mock
  private lateinit var bidManager: BidManager

  @Mock
  private lateinit var bidLifecycleListener: BidLifecycleListener

  @Mock
  private lateinit var cacheAdUnit: CacheAdUnit

  @Mock
  private lateinit var consentData: ConsentData

  @InjectMocks
  private lateinit var liveCdbCallListener: LiveCdbCallListener

  @Mock
  private lateinit var cdbRequest: CdbRequest

  @Mock
  private lateinit var cdbResponse: CdbResponse

  @Mock
  private lateinit var freshCdbResponseSlot: CdbResponseSlot

  @Mock
  private lateinit var cachedCdbResponseSlot: CdbResponseSlot

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun onBidResponse_givenValidResponseServedWithinTimeBudget_ThenDontCache_AndPassTheResponseThrough() {
    whenever(freshCdbResponseSlot.isValid()).thenReturn(true)
    whenever(cdbResponse.slots).thenReturn(listOf(freshCdbResponseSlot))
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)
    whenever(bidManager.isBidCurrentlySilent(freshCdbResponseSlot)).thenReturn(false)

    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)

    verify(bidManager).setTimeToNextCall(1_000)
    verify(bidManager, never()).setCacheAdUnits(any())
    verify(bidListener, never()).onNoBid()
    verify(bidListener, times(1)).onBidResponse(freshCdbResponseSlot)
    verify(bidLifecycleListener).onCdbCallFinished(cdbRequest, cdbResponse)
    verify(bidLifecycleListener).onBidConsumed(cacheAdUnit, freshCdbResponseSlot)
  }

  @Test
  fun onBidResponse_givenInvalidResponseServedWithinTimeBudget_ThenDontCache_AndCallNoBid() {
    whenever(freshCdbResponseSlot.isValid()).thenReturn(false)
    whenever(cdbResponse.slots).thenReturn(listOf(freshCdbResponseSlot))
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)
    whenever(bidManager.isBidCurrentlySilent(freshCdbResponseSlot)).thenReturn(false)

    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)

    verify(bidManager).setTimeToNextCall(1_000)
    verify(bidManager, never()).setCacheAdUnits(any())
    verify(bidListener).onNoBid()
    verify(bidListener, never()).onBidResponse(freshCdbResponseSlot)
    verify(bidLifecycleListener, never()).onBidConsumed(cacheAdUnit, freshCdbResponseSlot)
    verify(bidLifecycleListener).onCdbCallFinished(cdbRequest, cdbResponse)
  }

  @Test
  fun onBidResponse_givenEmptyResponseServedWithinTimeBudget_ThenDontCache_AndTriggerNoBid() {
    whenever(cdbResponse.slots).thenReturn(listOf())
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)
    whenever(bidManager.isBidCurrentlySilent(freshCdbResponseSlot)).thenReturn(false)

    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)

    verify(bidManager).setTimeToNextCall(1_000)
    verify(bidManager, never()).setCacheAdUnits(any())
    verify(bidListener, never()).onBidResponse(any())
    verify(bidListener).onNoBid()
    verify(bidLifecycleListener).onCdbCallFinished(cdbRequest, cdbResponse)
  }

  @Test
  fun onBidResponse_givenSilentBidServedWithinTimeBudget_ThenCache_AndTriggerNoBid() {
    whenever(cdbResponse.slots).thenReturn(listOf(freshCdbResponseSlot))
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)
    whenever(bidManager.isBidCurrentlySilent(freshCdbResponseSlot)).thenReturn(true)

    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)
    liveCdbCallListener.onTimeBudgetExceeded()

    verify(bidManager).setTimeToNextCall(1_000)
    verify(bidLifecycleListener).onCdbCallFinished(cdbRequest, cdbResponse)
    verify(bidListener, never()).onBidResponse(any())
    verify(bidManager).setCacheAdUnits(listOf(freshCdbResponseSlot))
    verify(bidListener).onNoBid()
  }

  @Test
  fun onBidResponse_givenTimeBudgetExceeded_NoTimeout_ThenConsumeCache_AndCacheNewResponse() {
    whenever(cdbResponse.slots).thenReturn(listOf(freshCdbResponseSlot))
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)

    liveCdbCallListener.onTimeBudgetExceeded()
    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)

    verify(bidManager).consumeCachedBid(cacheAdUnit, bidListener)
    verify(bidManager).setTimeToNextCall(1_000)
    verify(bidManager).setCacheAdUnits(cdbResponse.slots)
    verify(bidListener, never()).onBidResponse(freshCdbResponseSlot)
    verify(bidLifecycleListener).onCdbCallFinished(cdbRequest, cdbResponse)
    verify(bidLifecycleListener, never()).onBidConsumed(any(), any())
  }

  @Test
  fun onBidResponse_givenTimeout_ThenDontCacheAnythingAndConsumeCache() {
    whenever(cdbResponse.slots).thenReturn(listOf(freshCdbResponseSlot))
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)

    val exception = Exception()
    liveCdbCallListener.onTimeBudgetExceeded()
    liveCdbCallListener.onCdbError(cdbRequest, exception)

    verify(bidManager).consumeCachedBid(cacheAdUnit, bidListener)
    verify(bidManager, never()).setTimeToNextCall(1_000)
    verify(bidManager, never()).setCacheAdUnits(any())
    verify(bidListener, never()).onBidResponse(freshCdbResponseSlot)
    verify(bidLifecycleListener, never()).onCdbCallFinished(cdbRequest, cdbResponse)
    verify(bidLifecycleListener).onCdbCallFailed(cdbRequest, exception)
    verify(bidLifecycleListener, never()).onBidConsumed(any(), any())
  }

  @Test
  fun onBidResponse_givenNetworkErrorBeforeTimeBudgetExceeds_ThenDontCacheAnythingAndConsumeCache() {
    whenever(cdbResponse.slots).thenReturn(listOf(freshCdbResponseSlot))
    whenever(cdbResponse.timeToNextCall).thenReturn(1_000)

    val exception = Exception()
    liveCdbCallListener.onCdbError(cdbRequest, exception)
    liveCdbCallListener.onTimeBudgetExceeded()

    verify(bidManager).consumeCachedBid(cacheAdUnit, bidListener)
    verify(bidManager, never()).setTimeToNextCall(1_000)
    verify(bidManager, never()).setCacheAdUnits(any())
    verify(bidListener, never()).onBidResponse(freshCdbResponseSlot)
    verify(bidLifecycleListener, never()).onCdbCallFinished(cdbRequest, cdbResponse)
    verify(bidLifecycleListener).onCdbCallFailed(cdbRequest, exception)
    verify(bidLifecycleListener, never()).onBidConsumed(any(), any())
    verify(bidLifecycleListener).onCdbCallFailed(cdbRequest, exception)
  }

  @Test
  fun onBidResponse_givenConsentGiven_ThenUpdateConsentDataAccordingly() {
    whenever(cdbResponse.consentGiven).thenReturn(true)

    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)

    verify(consentData).setConsentGiven(true)
  }

  @Test
  fun onBidResponse_givenConsentNotGiven_ThenUpdateConsentDataAccordingly() {
    whenever(cdbResponse.consentGiven).thenReturn(false)

    liveCdbCallListener.onCdbResponse(cdbRequest, cdbResponse)

    verify(consentData).setConsentGiven(false)
  }
}
