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

import com.criteo.publisher.Bid
import com.criteo.publisher.integration.Integration
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class HeaderBiddingTest {

  @Mock
  private lateinit var integrationRegistry: IntegrationRegistry

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun enrichBid_GivenNullAdObject_DoNothing() {
    val handler = mock<HeaderBiddingHandler>()
    val headerBidding = HeaderBidding(listOf(handler), integrationRegistry)
    val bid = mock<Bid>()

    headerBidding.enrichBid(null, bid)

    verifyZeroInteractions(bid)
    verifyZeroInteractions(handler)
    verifyZeroInteractions(integrationRegistry)
  }

  @Test
  fun enrichBid_GivenHandlerAcceptingObjectNullBid_CleanAdObjectAndReturn() {
    val adObject = mock<Any>()

    val handler = givenHandler(adObject, true, Integration.STANDALONE)
    val headerBidding = HeaderBidding(listOf(handler), integrationRegistry)

    headerBidding.enrichBid(adObject, null)

    verify(handler).cleanPreviousBid(adObject)
    verify(handler, never()).enrichBid(any(), anyOrNull(), any())
    verify(integrationRegistry).declare(Integration.STANDALONE)
  }

  @Test
  fun enrichBid_GivenHandlerAcceptingObjectButNoBid_CleanBidAndReturn() {
    val obj = mock<Any>()
    val bid = mock<Bid>() {
      on { consumeSlot() } doReturn null
    }

    val handler = givenHandler(obj, true, Integration.STANDALONE)

    val headerBidding = HeaderBidding(listOf(handler), integrationRegistry)

    headerBidding.enrichBid(obj, bid)

    verify(handler).cleanPreviousBid(obj)
    verify(handler, never()).enrichBid(any(), anyOrNull(), any())
    verify(integrationRegistry).declare(Integration.STANDALONE)
  }

  @Test
  fun enrichBid_GivenManyHandlerAndBid_EnrichWithFirstAcceptingHandler() {
    val obj = mock<Any>()
    val slot = mock<CdbResponseSlot>()
    val bid = mock<Bid>() {
      on { adUnitType } doReturn CRITEO_CUSTOM_NATIVE
      on { consumeSlot() } doReturn slot
    }
    val handler1 = givenHandler(obj, false)
    val handler2 = givenHandler(obj, true, Integration.IN_HOUSE)
    val handler3 = givenHandler(obj, true, Integration.STANDALONE)

    val headerBidding = HeaderBidding(
        listOf(handler1, handler2, handler3),
        integrationRegistry
    )

    headerBidding.enrichBid(obj, bid)

    verify(handler1, never()).cleanPreviousBid(any())
    verify(handler1, never()).enrichBid(any(), anyOrNull(), any())
    verify(handler2).cleanPreviousBid(obj)
    verify(handler2).enrichBid(obj, CRITEO_CUSTOM_NATIVE, slot)
    verify(handler3, never()).cleanPreviousBid(any())
    verify(handler3, never()).enrichBid(any(), anyOrNull(), any())
    verify(integrationRegistry).declare(Integration.IN_HOUSE)
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
