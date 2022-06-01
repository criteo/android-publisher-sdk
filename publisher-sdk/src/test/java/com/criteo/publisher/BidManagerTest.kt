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

import com.criteo.publisher.config.Config
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.AdUnit
import com.criteo.publisher.model.CdbResponseSlot
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

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
    val bidListener = mock<BidListener>()
    val contextData = mock<ContextData>()

    doAnswer {
      it.getArgument<BidListener>(2).onBidResponse(expected)
    }.whenever(bidManager).getLiveBidForAdUnit(adUnit, contextData, bidListener)

    bidManager.getBidForAdUnit(adUnit, contextData, bidListener)

    verify(bidListener).onBidResponse(expected)
    verifyNoMoreInteractions(bidListener)
  }

  @Test
  fun getBidForAdUnit_GivenLiveBiddingEnabledAndNoResponseReturned_ThenTriggerNoBid() {
    whenever(config.isLiveBiddingEnabled).thenReturn(true)

    val adUnit = mock<AdUnit>()
    val bidListener = mock<BidListener>()
    val contextData = mock<ContextData>()

    doAnswer {
      it.getArgument<BidListener>(2).onNoBid()
    }.whenever(bidManager).getLiveBidForAdUnit(adUnit, contextData, bidListener)

    bidManager.getBidForAdUnit(adUnit, contextData, bidListener)

    verify(bidListener).onNoBid()
    verifyNoMoreInteractions(bidListener)
  }

  @Test
  fun getBidForAdUnit_GivenLiveBiddingDisabledAndCacheHit_ThenTriggerBidResponse() {
    whenever(config.isLiveBiddingEnabled).thenReturn(false)

    val adUnit = mock<AdUnit>()
    val expected = mock<CdbResponseSlot>()
    val bidListener = mock<BidListener>()
    val contextData = mock<ContextData>()

    doReturn(expected).whenever(bidManager).getBidForAdUnitAndPrefetch(adUnit, contextData)

    bidManager.getBidForAdUnit(adUnit, contextData, bidListener)

    verify(bidListener).onBidResponse(expected)
    verifyNoMoreInteractions(bidListener)
  }

  @Test
  fun getBidForAdUnit_GivenLiveBiddingDisabledAndCacheMiss_ThenTriggerNoBid() {
    whenever(config.isLiveBiddingEnabled).thenReturn(false)

    val adUnit = mock<AdUnit>()
    val bidListener = mock<BidListener>()
    val contextData = mock<ContextData>()

    doReturn(null).whenever(bidManager).getBidForAdUnitAndPrefetch(adUnit, contextData)

    bidManager.getBidForAdUnit(adUnit, contextData, bidListener)

    verify(bidListener).onNoBid()
    verifyNoMoreInteractions(bidListener)
  }
}
