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

import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class RemoteHandlerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var sendingQueue: RemoteLogSendingQueue

  @MockBean
  private lateinit var remoteLogRecordsFactory: RemoteLogRecordsFactory

  @Inject
  private lateinit var remoteHandler: RemoteHandler

  @Test
  fun log_GivenNoRemoteLogFromFactory_DoesNothing() {
    val logMessage = LogMessage(message = null)

    whenever(remoteLogRecordsFactory.createLogRecords(logMessage)).thenReturn(null)

    remoteHandler.log("tag", logMessage)

    verifyZeroInteractions(sendingQueue)
  }

  @Test
  fun log_GivenRemoteLogFromFactory_PushItInSendingQueue() {
    val logMessage = LogMessage(message = null)
    val logRecords = mock<RemoteLogRecords>()

    whenever(remoteLogRecordsFactory.createLogRecords(logMessage)).thenReturn(logRecords)

    remoteHandler.log("tag", logMessage)

    verify(sendingQueue).offer(logRecords)
  }
}
