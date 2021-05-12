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

package com.criteo.publisher.logging

import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.SafeRunnable
import com.criteo.publisher.TestAdUnits
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.Config
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.privacy.ConsentData
import com.criteo.publisher.util.BuildConfigWrapper
import com.dummypublisher.DummyPublisherCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RemoteLoggingFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @SpyBean
  private lateinit var consentData: ConsentData

  @SpyBean
  private lateinit var remoteLogSendingQueueConsumer: RemoteLogSendingQueueConsumer

  @SpyBean
  private lateinit var remoteLogSendingQueue: RemoteLogSendingQueue

  @SpyBean
  private lateinit var api: PubSdkApi

  @SpyBean
  private lateinit var config: Config

  @Before
  fun setUp() {
    whenever(consentData.isConsentGiven()).thenReturn(true)
  }

  @Test
  fun whenCriteoInitIsCalledFromMainThread_AllLogsHaveDeviceId() {
    whenever(config.remoteLogLevel).thenReturn(RemoteLogRecords.RemoteLogLevel.INFO)

    runOnMainThreadAndWait {
      givenInitializedCriteo()
    }
    mockedDependenciesRule.waitForIdleState()

    // Get a new bid to trigger sending of remote logs
    Criteo.getInstance().loadBid(TestAdUnits.INTERSTITIAL) {
      // ignored
    }
    mockedDependenciesRule.waitForIdleState()

    verify(api).postLogs(check {
      assertThat(it.map { it.context.deviceId }).isNotEmpty.doesNotContainNull()
    })
  }

  @Test
  fun whenCriteoInitIsCalled_SendRemoteLogBatch() {
    givenInitializedCriteo()
    mockedDependenciesRule.waitForIdleState()

    verify(remoteLogSendingQueueConsumer).sendRemoteLogBatch()
  }

  @Test
  fun whenPublisherExceptionIsCaughtBySafeRunnable_DontShowPublisherSecrets() {
    doReturn(false).whenever(buildConfigWrapper).preconditionThrowsOnException()

    val safeRunnable = object : SafeRunnable() {
      override fun runSafely() {
        DummyPublisherCode.sdkDummyInterfaceThrowingGenericException().foo()
      }
    }

    safeRunnable.run()
    mockedDependenciesRule.waitForIdleState()

    verify(remoteLogSendingQueue).offer(check {
      assertThat(it.context.exceptionType).isEqualTo("ExecutionException")
      assertThat(it.logRecords[0].messages[0])
          .doesNotContain(DummyPublisherCode.secrets)
          .doesNotContain(DummyPublisherCode.javaClass.`package`!!.name)
    })
  }
}
