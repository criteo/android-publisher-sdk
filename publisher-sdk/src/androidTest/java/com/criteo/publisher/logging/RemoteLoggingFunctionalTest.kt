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

import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.SafeRunnable
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.BuildConfigWrapper
import com.dummypublisher.DummyPublisherCode
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class RemoteLoggingFunctionalTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @SpyBean
  private lateinit var remoteLogSendingQueueConsumer: RemoteLogSendingQueueConsumer

  @SpyBean
  private lateinit var remoteLogSendingQueue: RemoteLogSendingQueue

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
