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

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdUnit
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.model.Config
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test

class BidManagerTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var config: Config

  @SpyBean
  private lateinit var bidManager: BidManager

  @Test
  fun getBidForAdUnit_GivenLiveBiddingEnabledWithAResponse_ThenTriggerBidResponse() {
    whenever(config.isLiveBiddingEnabled).thenReturn(true)

    val adUnit = mock<AdUnit>()
    val expected = mock<CdbResponseSlot>()
    val bidListener = mock<CdbResponseSlotListener>()

    doAnswer {
      it.getArgument<CdbResponseSlotListener>(1).onBidResponse(expected)
    }.whenever(bidManager).getLiveBidForAdUnit(adUnit, bidListener)

    bidManager.getBidForAdUnit(adUnit, bidListener)

    verify(bidListener).onBidResponse(expected)
    verifyNoMoreInteractions(bidListener)
  }

  @Test
  fun getBidForAdUnit_GivenLiveBiddingEnabledAndNoResponseReturned_ThenTriggerNoBid() {
    whenever(config.isLiveBiddingEnabled).thenReturn(true)

    val adUnit = mock<AdUnit>()
    val bidListener = mock<CdbResponseSlotListener>()

    doAnswer {
      it.getArgument<CdbResponseSlotListener>(1).onNoBid()
    }.whenever(bidManager).getLiveBidForAdUnit(adUnit, bidListener)

    bidManager.getBidForAdUnit(adUnit, bidListener)

    verify(bidListener).onNoBid()
    verifyNoMoreInteractions(bidListener)
  }

  @Test
  fun getBidForAdUnit_GivenLiveBiddingDisabledAndCacheHit_ThenTriggerBidResponse() {
    whenever(config.isLiveBiddingEnabled).thenReturn(false)

    val adUnit = mock<AdUnit>()
    val expected = mock<CdbResponseSlot>()
    val bidListener = mock<CdbResponseSlotListener>()

    doReturn(expected).whenever(bidManager).getBidForAdUnitAndPrefetch(adUnit)

    bidManager.getBidForAdUnit(adUnit, bidListener)

    verify(bidListener).onBidResponse(expected)
    verifyNoMoreInteractions(bidListener)
  }

  @Test
  fun getBidForAdUnit_GivenLiveBiddingDisabledAndCacheMiss_ThenTriggerNoBid() {
    whenever(config.isLiveBiddingEnabled).thenReturn(false)

    val adUnit = mock<AdUnit>()
    val bidListener = mock<CdbResponseSlotListener>()

    doReturn(null).whenever(bidManager).getBidForAdUnitAndPrefetch(adUnit)

    bidManager.getBidForAdUnit(adUnit, bidListener)

    verify(bidListener).onNoBid()
    verifyNoMoreInteractions(bidListener)
  }
}
