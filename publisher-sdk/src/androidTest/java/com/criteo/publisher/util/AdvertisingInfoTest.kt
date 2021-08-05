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
package com.criteo.publisher.util

import android.content.Context
import com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor
import javax.inject.Inject

class AdvertisingInfoTest {

  private companion object {
    const val DEVICE_ID_LIMITED = "00000000-0000-0000-0000-000000000000"
  }

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var context: Context

  @Inject
  private lateinit var executor: Executor

  @SpyBean
  private lateinit var advertisingInfo: AdvertisingInfo

  @Test
  fun prefetchAsync_FromWorkerThread_DispatchAsync() {
    val initialThread = Thread.currentThread()

    whenever(advertisingInfo.advertisingId).then {
      assertThat(Thread.currentThread()).isNotEqualTo(initialThread)
      null
    }

    advertisingInfo.prefetchAsync()
    mockedDependenciesRule.waitForIdleState()

    verify(advertisingInfo).advertisingId
  }

  @Test
  fun prefetchAsync_FromMainThread_DispatchAsync() {
    lateinit var initialThread: Thread

    whenever(advertisingInfo.advertisingId).then {
      assertThat(Thread.currentThread()).isNotEqualTo(initialThread)
      null
    }

    runOnMainThreadAndWait {
      initialThread = Thread.currentThread()
      advertisingInfo.prefetchAsync()
    }
    mockedDependenciesRule.waitForIdleState()

    verify(advertisingInfo).advertisingId
  }

  @Test
  fun getAdvertisingId_GivenPlayServiceAdsIdentifierInClasspath_ReturnNonNull() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val advertisingId = advertisingInfo.advertisingId

    assertThat(advertisingId).isNotEmpty().isNotEqualTo(DEVICE_ID_LIMITED)
  }

  @Test
  fun getAdvertisingId_CalledFromUiThread_ReturnNull() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val advertisingId = callOnMainThreadAndWait {
      advertisingInfo.advertisingId
    }

    assertThat(advertisingId).isNull()
  }

  @Test
  fun getAdvertisingId_CalledFromUiThreadAfterBeingCalledOnWorkerThread_ReturnNonNull() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val advertisingIdFromWorkerThread = advertisingInfo.advertisingId
    val advertisingIdFromMainThread = callOnMainThreadAndWait {
      advertisingInfo.advertisingId
    }

    assertThat(advertisingIdFromWorkerThread).isNotEmpty()
        .isNotEqualTo(DEVICE_ID_LIMITED)
        .isEqualTo(advertisingIdFromMainThread)
  }

  @Test
  fun getAdvertisingId_CalledFromUiThreadAfterAFinishedPrefetch_ReturnNonNull() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    advertisingInfo.prefetchAsync()
    mockedDependenciesRule.waitForIdleState()

    val advertisingId = callOnMainThreadAndWait {
      advertisingInfo.advertisingId
    }

    assertThat(advertisingId).isNotEmpty().isNotEqualTo(DEVICE_ID_LIMITED)
  }

  @Test
  fun getAdvertisingId_CalledFromUiThreadAfterAFinishedPrefetchFromMainThread_ReturnNonNull() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    runOnMainThreadAndWait {
      advertisingInfo.prefetchAsync()
    }
    mockedDependenciesRule.waitForIdleState()

    val advertisingId = callOnMainThreadAndWait {
      advertisingInfo.advertisingId
    }

    assertThat(advertisingId).isNotEmpty().isNotEqualTo(DEVICE_ID_LIMITED)
  }

  @Test
  fun getAdvertisingId_GivenLimitedAdTracking_ReturnLimitedDeviceId() {
    val advertisingIdClient = spy(AdvertisingInfo.SafeAdvertisingIdClient()) {
      doReturn(true).whenever(mock).isLimitAdTrackingEnabled(any())
    }
    advertisingInfo = AdvertisingInfo(context, executor, advertisingIdClient)

    val advertisingId = advertisingInfo.advertisingId

    assertThat(advertisingId).isEqualTo(DEVICE_ID_LIMITED)
  }

  @Test
  fun isLimitAdTrackingEnabled_GivenPlayServiceAdsIdentifierInClasspath_ReturnFalse() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val isLimitAdTrackingEnabled = advertisingInfo.isLimitAdTrackingEnabled

    assertThat(isLimitAdTrackingEnabled).isFalse()
  }

  @Test
  fun isLimitAdTrackingEnabled_CalledFromUiThread_ReturnFalse() {
    // Assume that the advertising ID is available, this is a case on new clean emulators/devices

    val isLimitAdTrackingEnabled = callOnMainThreadAndWait {
      advertisingInfo.isLimitAdTrackingEnabled
    }

    assertThat(isLimitAdTrackingEnabled).isFalse()
  }
}
