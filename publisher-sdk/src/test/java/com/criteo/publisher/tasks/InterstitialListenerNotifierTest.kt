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

package com.criteo.publisher.tasks

import com.criteo.publisher.CriteoErrorCode
import com.criteo.publisher.CriteoInterstitial
import com.criteo.publisher.CriteoInterstitialAdListener
import com.criteo.publisher.CriteoListenerCode
import com.criteo.publisher.CriteoListenerCode.CLICK
import com.criteo.publisher.CriteoListenerCode.CLOSE
import com.criteo.publisher.CriteoListenerCode.INVALID
import com.criteo.publisher.CriteoListenerCode.INVALID_CREATIVE
import com.criteo.publisher.CriteoListenerCode.OPEN
import com.criteo.publisher.CriteoListenerCode.VALID
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class InterstitialListenerNotifierTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Mock
  private lateinit var interstitial: CriteoInterstitial

  @Mock
  private lateinit var listener: CriteoInterstitialAdListener

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  private lateinit var listenerRef: Reference<CriteoInterstitialAdListener>

  private val runOnUiThreadExecutor = DirectMockRunOnUiThreadExecutor()

  private lateinit var listenerNotifier: InterstitialListenerNotifier

  @Before
  fun setUp() {
    setUpExpectingListenerToBeInvokedInExecutor()
  }

  @After
  fun tearDown() {
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun notifyForValid_GivenReachableListener_NotifyItForAdReceived() {
    givenReachableListenerReference()

    listenerNotifier.notifyFor(VALID)

    verify(listener).onAdReceived(interstitial)
  }

  @Test
  fun notifyForInvalid_GivenReachableListener_NotifyItForNoFill() {
    givenReachableListenerReference()

    listenerNotifier.notifyFor(INVALID)

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL)
  }

  @Test
  fun notifyForInvalidCreative_GivenReachableListener_NotifyItForNetworkError() {
    givenReachableListenerReference()

    listenerNotifier.notifyFor(INVALID_CREATIVE)

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR)
  }

  @Test
  fun notifyForClick_GivenReachableListener_NotifyItForClickAndUserLeavingApp() {
    givenReachableListenerReference()

    listenerNotifier.notifyFor(CLICK)

    verify(listener).onAdClicked()
    verify(listener).onAdLeftApplication()
  }

  @Test
  fun notifyForOpen_GivenReachableListener_NotifyItForAdOpened() {
    givenReachableListenerReference()

    listenerNotifier.notifyFor(OPEN)

    verify(listener).onAdOpened()
  }

  @Test
  fun notifyForClose_GivenReachableListener_NotifyItForAdClosed() {
    givenReachableListenerReference()

    listenerNotifier.notifyFor(CLOSE)

    verify(listener).onAdClosed()
  }

  @Test
  fun notifyFor_GivenUnreachableListener_DoNothing() {
    givenUnreachableListenerReference()

    CriteoListenerCode.values().forEach {
      assertThatCode {
        listenerNotifier.notifyFor(it)
      }.doesNotThrowAnyException()
    }
  }

  @Test
  fun notifyFor_GivenThrowingListener_DoNothing() {
    givenThrowingListener()
    // by default exception is thrown in debug mode but we want to test
    // if it is working fine when debug throw disabled
    doReturn(false).whenever(buildConfigWrapper).preconditionThrowsOnException()

    CriteoListenerCode.values().forEach {
      assertThatCode {
        listenerNotifier.notifyFor(it)
      }.doesNotThrowAnyException()
    }
  }

  private fun givenReachableListenerReference() {
    listenerRef = WeakReference(listener)
    createInterstitialListenerNotifier()
  }

  private fun givenUnreachableListenerReference() {
    listenerRef = WeakReference(null)
    createInterstitialListenerNotifier()
  }

  private fun createInterstitialListenerNotifier() {
    listenerNotifier = InterstitialListenerNotifier(
        interstitial,
        listenerRef,
        runOnUiThreadExecutor
    )
  }

  private fun givenThrowingListener() {
    givenReachableListenerReference()

    doThrow(RuntimeException::class).whenever(listener).onAdReceived(any())
    doThrow(RuntimeException::class).whenever(listener).onAdFailedToReceive(any())
    doThrow(RuntimeException::class).whenever(listener).onAdClicked()
    doThrow(RuntimeException::class).whenever(listener).onAdLeftApplication()
    doThrow(RuntimeException::class).whenever(listener).onAdClosed()
    doThrow(RuntimeException::class).whenever(listener).onAdOpened()
  }

  private fun setUpExpectingListenerToBeInvokedInExecutor() {
    doAnswer {
      runOnUiThreadExecutor.expectIsRunningInExecutor()
    }.whenever(listener).onAdReceived(any())

    doAnswer {
      runOnUiThreadExecutor.expectIsRunningInExecutor()
    }.whenever(listener).onAdFailedToReceive(any())

    doAnswer {
      runOnUiThreadExecutor.expectIsRunningInExecutor()
    }.whenever(listener).onAdClicked()

    doAnswer {
      runOnUiThreadExecutor.expectIsRunningInExecutor()
    }.whenever(listener).onAdLeftApplication()

    doAnswer {
      runOnUiThreadExecutor.expectIsRunningInExecutor()
    }.whenever(listener).onAdClosed()

    doAnswer {
      runOnUiThreadExecutor.expectIsRunningInExecutor()
    }.whenever(listener).onAdOpened()
  }
}
