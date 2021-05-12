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

import android.util.Log
import com.criteo.publisher.dependency.LazyDependency
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.model.Config
import com.criteo.publisher.privacy.ConsentData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.atMost
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import javax.inject.Inject

class RemoteHandlerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var config: Config

  @MockBean
  private lateinit var sendingQueue: RemoteLogSendingQueue

  @MockBean
  private lateinit var remoteLogRecordsFactory: RemoteLogRecordsFactory

  @MockBean
  private lateinit var consentData: ConsentData

  @Inject
  private lateinit var remoteHandler: RemoteHandler

  @Before
  fun setUp() {
    whenever(config.remoteLogLevel).doReturn(RemoteLogLevel.DEBUG)
    whenever(consentData.isConsentGiven()).thenReturn(true)
  }

  @Test
  fun log_GivenConfiguredRemoteLogLevel_OnlyPushMessageWithLogLevelAboveOrEqual() {
    val info = LogMessage(level = Log.INFO, message = "dummy")
    val warning = LogMessage(level = Log.WARN, message = "dummy")
    val error = LogMessage(level = Log.ERROR, message = "dummy")

    whenever(config.remoteLogLevel).doReturn(RemoteLogLevel.WARNING)

    val logRecords = mock<RemoteLogRecords>()
    whenever(remoteLogRecordsFactory.createLogRecords(any())).thenReturn(logRecords)

    remoteHandler.log("tag", info)
    remoteHandler.log("tag", warning)
    remoteHandler.log("tag", error)
    mockedDependenciesRule.waitForIdleState()

    verify(sendingQueue, times(2)).offer(any())
    verify(remoteLogRecordsFactory).createLogRecords(warning)
    verify(remoteLogRecordsFactory).createLogRecords(error)
    verifyNoMoreInteractions(remoteLogRecordsFactory)
  }

  @Test
  fun log_GivenNoRemoteLogFromFactory_DoesNothing() {
    val logMessage = LogMessage(message = null)

    whenever(remoteLogRecordsFactory.createLogRecords(logMessage)).thenReturn(null)

    remoteHandler.log("tag", logMessage)
    mockedDependenciesRule.waitForIdleState()

    verifyZeroInteractions(sendingQueue)
  }

  @Test
  fun log_GivenRemoteLogFromFactory_PushItInSendingQueue() {
    val logMessage = LogMessage(message = null)
    val logRecords = mock<RemoteLogRecords>()

    whenever(remoteLogRecordsFactory.createLogRecords(logMessage)).thenReturn(logRecords)

    remoteHandler.log("tag", logMessage)
    mockedDependenciesRule.waitForIdleState()

    verify(sendingQueue).offer(logRecords)
  }

  @Test
  fun log_GivenConsentNotGiven_DoesNothing() {
    whenever(consentData.isConsentGiven()).thenReturn(false)
    val logMessage = LogMessage(message = null)

    remoteHandler.log("tag", logMessage)
    mockedDependenciesRule.waitForIdleState()

    verifyZeroInteractions(remoteLogRecordsFactory)
    verifyZeroInteractions(sendingQueue)
  }

  @Test
  fun log_GivenSendingQueueLogging_StopRecursion() {
    remoteHandler = spy(remoteHandler) {
      on { isMainThread() } doReturn true doReturn false
    }

    val logRecords = mock<RemoteLogRecords>()
    whenever(remoteLogRecordsFactory.createLogRecords(any())).thenReturn(logRecords)

    val logger = Logger("tag", listOf(LazyDependency { remoteHandler as LogHandler }))

    doAnswer {
      logger.debug("dummy")
      true
    }.whenever(sendingQueue).offer(any())

    logger.debug("dummy")
    mockedDependenciesRule.waitForIdleState()

    verify(sendingQueue, atLeastOnce()).offer(any())
    verify(sendingQueue, atMost(3)).offer(any())
  }
}
