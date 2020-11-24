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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class LoggerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var handler1: LogHandler

  @Mock
  private lateinit var handler2: LogHandler

  private lateinit var logger: Logger

  @Before
  fun setUp() {
    logger = spy(Logger("myTag", listOf(handler1, handler2)))
  }

  @Test
  fun debug_GivenMessageAndArgs_DelegateFormattedMessage() {
    logger.debug("Hello %s", "World")

    verify(handler1).log("myTag", LogMessage(Log.DEBUG, "Hello World"))
    verify(handler2).log("myTag", LogMessage(Log.DEBUG, "Hello World"))
  }

  @Test
  fun debug_GivenMessageAndThrowable_DelegateMessageAndThrowable() {
    val exception = Exception()
    logger.debug("Hello", exception)

    verify(handler1).log("myTag", LogMessage(Log.DEBUG, "Hello", exception))
    verify(handler2).log("myTag", LogMessage(Log.DEBUG, "Hello", exception))
  }

  @Test
  fun info_GivenMessageAndThrowable_DelegateMessageAndThrowable() {
    val exception = Exception()

    logger.info("Hello", exception)

    verify(handler1).log("myTag", LogMessage(Log.INFO, "Hello", exception))
    verify(handler2).log("myTag", LogMessage(Log.INFO, "Hello", exception))
  }

  @Test
  fun info_GivenMessageAndArgs_PrintFormattedMessage() {
    logger.info("Hello %s", "World")

    verify(handler1).log("myTag", LogMessage(Log.INFO, "Hello World"))
    verify(handler2).log("myTag", LogMessage(Log.INFO, "Hello World"))
  }

  @Test
  fun warning_GivenMessageAndArgs_PrintFormattedMessage() {
    logger.warning("Hello %s", "World")

    verify(handler1).log("myTag", LogMessage(Log.WARN, "Hello World"))
    verify(handler2).log("myTag", LogMessage(Log.WARN, "Hello World"))
  }

  @Test
  fun error_GivenJustThrowable_DelegateOnlyThrowable() {
    val exception = Exception()

    logger.error(exception)

    verify(handler1).log("myTag", LogMessage(Log.ERROR, null, exception))
    verify(handler2).log("myTag", LogMessage(Log.ERROR, null, exception))
  }

  @Test
  fun error_GivenThrowableAndMessage_PrintMessageThenStacktrace() {
    val exception = Exception()

    logger.error("Hello", exception)

    verify(handler1).log("myTag", LogMessage(Log.ERROR, "Hello", exception))
    verify(handler2).log("myTag", LogMessage(Log.ERROR, "Hello", exception))
  }

  @Test
  fun log_GivenOneHandlerThrowing_IgnoreErrorAndKeepLoggingWithOtherHandler() {
    whenever(handler1.log(any(), any())).thenThrow(Exception::class.java)
    val logMessage = LogMessage(Log.INFO, "message")

    logger.log(logMessage)

    verify(handler2).log("myTag", logMessage)
  }
}
