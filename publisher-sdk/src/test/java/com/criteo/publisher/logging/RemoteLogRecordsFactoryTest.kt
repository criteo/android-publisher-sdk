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

import android.content.Context
import android.util.Log
import com.criteo.publisher.Clock
import com.criteo.publisher.Session
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogContext
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogRecord
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class RemoteLogRecordsFactoryTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @SpyBean
  private lateinit var context: Context

  @MockBean
  private lateinit var advertisingInfo: AdvertisingInfo

  @MockBean
  private lateinit var session: Session

  @MockBean
  private lateinit var integrationRegistry: IntegrationRegistry

  @MockBean
  private lateinit var clock: Clock

  @SpyBean
  private lateinit var factory: RemoteLogRecordsFactory

  @Test
  fun createLogRecord_GivenLogWithoutMessageOrThrowable_ReturnNull() {
    val logMessage = LogMessage(message = null, throwable = null)

    val logRecords = factory.createLogRecords(logMessage)

    assertThat(logRecords).isNull()
  }

  @Test
  fun createLogRecord_GivenLogWithUnknownLogLevel_ReturnNull() {
    val logMessage = LogMessage(level = 42, message = null)

    val logRecords = factory.createLogRecords(logMessage)

    assertThat(logRecords).isNull()
  }

  @Test
  fun createLogRecord_GivenValidLog_ReturnLogRecords() {
    val timestamp = ZonedDateTime.of(2042, 6, 22, 13, 37, 28, 2, ZoneOffset.UTC).toInstant().toEpochMilli()

    whenever(buildConfigWrapper.sdkVersion).doReturn("1.2.3")
    whenever(context.packageName).doReturn("org.dummy")
    whenever(advertisingInfo.advertisingId).doReturn("device-id")
    whenever(session.sessionId).doReturn("session-id")
    whenever(integrationRegistry.profileId).doReturn(42)
    whenever(clock.currentTimeInMillis).doReturn(timestamp)

    val throwable = UnsupportedOperationException()
    val logMessage = LogMessage(Log.WARN, "`message of log`", throwable)

    doReturn("`throwable message+stacktrace`").whenever(factory).getStackTraceString(throwable)
    doReturn("thread-name").whenever(factory).getCurrentThreadName()

    val logRecords = factory.createLogRecords(logMessage)

    val expectedMessage = "`message of log`,`throwable message+stacktrace`,threadId:thread-name,2042-06-22T13:37:28Z"
    assertThat(logRecords).isEqualTo(RemoteLogRecords(
        RemoteLogContext(
            "1.2.3",
            "org.dummy",
            "device-id",
            "session-id",
        42,
            "UnsupportedOperationException"
        ),
        listOf(RemoteLogRecord(RemoteLogLevel.WARNING, listOf(expectedMessage)))
    ))
  }

  @Test
  fun createMessageBody_GivenNullMessageAndNullThrowable_ReturnNoMessage() {
    val logMessage = LogMessage(message = null, throwable = null)

    val messageBody = factory.createMessageBody(logMessage)

    assertThat(messageBody).isNull()
  }

  @Test
  fun createMessageBody_GivenOnlyMessage_FormatItWithoutStacktrace() {
    val timestamp = ZonedDateTime.of(2042, 6, 22, 13, 37, 28, 2, ZoneOffset.UTC).toInstant().toEpochMilli()
    whenever(clock.currentTimeInMillis).doReturn(timestamp)
    doReturn("thread-name").whenever(factory).getCurrentThreadName()

    val logMessage = LogMessage(message = "dummy message", throwable = null)

    val messageBody = factory.createMessageBody(logMessage)

    assertThat(messageBody).isEqualTo("dummy message,threadId:thread-name,2042-06-22T13:37:28Z")
  }

  @Test
  fun createMessageBody_GivenOnlyThrowable_FormatItWithStacktrace() {
    val throwable = NullPointerException()
    val timestamp = ZonedDateTime.of(2042, 6, 22, 13, 37, 28, 2, ZoneOffset.UTC).toInstant().toEpochMilli()
    whenever(clock.currentTimeInMillis).doReturn(timestamp)
    doReturn("thread-name").whenever(factory).getCurrentThreadName()
    doReturn("throwable message+stacktrace").whenever(factory).getStackTraceString(throwable)

    val logMessage = LogMessage(message = null, throwable = throwable)

    val messageBody = factory.createMessageBody(logMessage)

    assertThat(messageBody).isEqualTo("throwable message+stacktrace,threadId:thread-name,2042-06-22T13:37:28Z")
  }
}
