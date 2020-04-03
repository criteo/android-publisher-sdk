package com.criteo.publisher.bid

import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.model.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

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
    listener = CompositeBidLifecycleListener(listener1, listener2)
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
    val slot = mock<Slot>()

    listener.onBidConsumed(adUnit, slot)

    verify(listener1).onBidConsumed(adUnit, slot)
    verify(listener2).onBidConsumed(adUnit, slot)
  }
}