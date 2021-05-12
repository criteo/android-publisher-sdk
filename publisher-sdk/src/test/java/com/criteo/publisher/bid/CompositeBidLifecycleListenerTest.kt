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

package com.criteo.publisher.bid

import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.CacheAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbResponse
import com.criteo.publisher.model.CdbResponseSlot
import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CompositeBidLifecycleListenerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var listener1: BidLifecycleListener

  @Mock
  private lateinit var listener2: BidLifecycleListener

  private lateinit var listener: CompositeBidLifecycleListener

  @Before
  fun setUp() {
    listener = CompositeBidLifecycleListener().apply {
      add(listener1)
      add(listener2)
    }
  }

  @Test
  fun onSdkInitialized_GivenDelegates_DelegateToThem() {
    listener.onSdkInitialized()

    verify(listener1).onSdkInitialized()
    verify(listener2).onSdkInitialized()
  }

  @Test
  fun onCdbCallStarted_GivenDelegates_DelegateToThem() {
    val request = mock<CdbRequest>()

    listener.onCdbCallStarted(request)

    verify(listener1).onCdbCallStarted(request)
    verify(listener2).onCdbCallStarted(request)
  }

  @Test
  fun onCdbCallFinished_GivenDelegates_DelegateToThem() {
    val request = mock<CdbRequest>()
    val response = mock<CdbResponse>()

    listener.onCdbCallFinished(request, response)

    verify(listener1).onCdbCallFinished(request, response)
    verify(listener2).onCdbCallFinished(request, response)
  }

  @Test
  fun onCdbCallFailed_GivenDelegates_DelegateToThem() {
    val request = mock<CdbRequest>()
    val exception = mock<Exception>()

    listener.onCdbCallFailed(request, exception)

    verify(listener1).onCdbCallFailed(request, exception)
    verify(listener2).onCdbCallFailed(request, exception)
  }

  @Test
  fun onBidConsumed_GivenDelegates_DelegateToThem() {
    val adUnit = CacheAdUnit(AdSize(1, 2), "myAdUnit", CRITEO_BANNER)
    val slot = mock<CdbResponseSlot>()

    listener.onBidConsumed(adUnit, slot)

    verify(listener1).onBidConsumed(adUnit, slot)
    verify(listener2).onBidConsumed(adUnit, slot)
  }
}