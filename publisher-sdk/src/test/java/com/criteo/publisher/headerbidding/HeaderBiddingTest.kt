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

package com.criteo.publisher.headerbidding

import com.criteo.publisher.BidCompleteListener
import com.criteo.publisher.BidManager
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.model.AdUnit
import com.criteo.publisher.model.CdbResponseSlot
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class HeaderBiddingTest {

  @Mock
  private lateinit var bidManager: BidManager

  @Mock
  private lateinit var integrationRegistry: IntegrationRegistry

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun enrichBid_GivenNullObject_DoNothing() {
    val handler = mock<HeaderBiddingHandler>()
    val headerBidding = HeaderBidding(bidManager, listOf(handler), integrationRegistry)
    val bidListener = mock<BidCompleteListener>()
    headerBidding.enrichBid(null, mock(), bidListener)

    verifyZeroInteractions(bidManager)
    verifyZeroInteractions(handler)
    verifyZeroInteractions(integrationRegistry)
    verifyZeroInteractions(bidListener)
  }

  @Test
  fun enrichBid_GivenHandlerAcceptingObjectButNoBid_CleanBidAndReturn() {
    val obj = mock<Any>()
    val adUnit = mock<AdUnit>()
    val handler = givenHandler(obj, true, Integration.STANDALONE)

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn null
    }

    val headerBidding = HeaderBidding(bidManager, listOf(handler), integrationRegistry)

    headerBidding.enrichBid(obj, adUnit, mock())

    verify(handler).cleanPreviousBid(obj)
    verify(handler, never()).enrichBid(any(), anyOrNull(), any())
    verify(integrationRegistry).declare(Integration.STANDALONE)
  }

  @Test
  fun enrichBid_GivenManyHandlerAndBid_EnrichWithFirstAcceptingHandler() {
    val obj = mock<Any>()
    val adUnit = mock<AdUnit>()
    val slot = mock<CdbResponseSlot>()
    val handler1 = givenHandler(obj, false)
    val handler2 = givenHandler(obj, true, Integration.IN_HOUSE)
    val handler3 = givenHandler(obj, true, Integration.STANDALONE)

    bidManager.stub {
      on { getBidForAdUnitAndPrefetch(adUnit) } doReturn slot
    }

    val bidListener = mock<BidCompleteListener>()

    val headerBidding = HeaderBidding(
        bidManager,
        listOf(handler1, handler2, handler3),
        integrationRegistry
    )

    headerBidding.enrichBid(obj, adUnit, bidListener)

    verify(handler1, never()).cleanPreviousBid(any())
    verify(handler1, never()).enrichBid(any(), anyOrNull(), any())
    verify(handler2).cleanPreviousBid(obj)
    verify(handler2).enrichBid(obj, adUnit, slot)
    verify(handler3, never()).cleanPreviousBid(any())
    verify(handler3, never()).enrichBid(any(), anyOrNull(), any())
    verify(integrationRegistry).declare(Integration.IN_HOUSE)
    verify(bidListener).onBiddingComplete()
    verifyNoMoreInteractions(integrationRegistry)
  }

  private fun givenHandler(
      obj: Any,
      accepting: Boolean,
      integration: Integration = Integration.FALLBACK
  ): HeaderBiddingHandler {
    return mock {
      on { canHandle(obj) } doReturn accepting
      on { this.integration } doReturn integration
    }
  }

}