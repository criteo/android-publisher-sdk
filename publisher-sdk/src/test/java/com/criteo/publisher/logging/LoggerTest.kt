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
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
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
  fun debug_GivenOnlyThrowable_DelegateThrowable() {
    val exception = Exception()
    logger.debug(exception)

    verify(handler1).log("myTag", LogMessage(Log.DEBUG, null, exception))
    verify(handler2).log("myTag", LogMessage(Log.DEBUG, null, exception))
  }

  @Test
  fun log_GivenOneHandlerThrowing_IgnoreErrorAndKeepLoggingWithOtherHandler() {
    whenever(handler1.log(any(), any())).thenThrow(Exception::class.java)
    val logMessage = LogMessage(Log.INFO, "message")

    logger.log(logMessage)

    verify(handler2).log("myTag", logMessage)
  }

  @Test
  fun log_GivenLogsProducedWhileLogging_LogItOnlyOnceAndStopRecursion() {
    lateinit var logger: Logger

    val handler = mock<LogHandler>()

    handler1 = object : LogHandler {
      override fun log(tag: String, logMessage: LogMessage) {
        logger.debug("start handler1#log")
        handler.log(tag, logMessage.copy(message = "handler1: ${logMessage.message}"))
        logger.debug("end handler1#log")
      }
    }

    handler2 = object : LogHandler {
      override fun log(tag: String, logMessage: LogMessage) {
        logger.debug("start handler2#log")
        handler.log(tag, logMessage.copy(message = "handler2: ${logMessage.message}"))
        logger.debug("end handler2#log")
      }
    }

    logger = Logger("myTag", listOf(handler1, handler2))
    logger.log(LogMessage(message = "dummy message"))

    inOrder(handler) {
      verify(handler).log(any(), argThat { message == "handler1: start handler1#log" })
      verify(handler).log(any(), argThat { message == "handler2: start handler1#log" })
      verify(handler).log(any(), argThat { message == "handler1: dummy message" })
      verify(handler).log(any(), argThat { message == "handler1: end handler1#log" })
      verify(handler).log(any(), argThat { message == "handler2: end handler1#log" })

      verify(handler).log(any(), argThat { message == "handler1: start handler2#log" })
      verify(handler).log(any(), argThat { message == "handler2: start handler2#log" })
      verify(handler).log(any(), argThat { message == "handler2: dummy message" })
      verify(handler).log(any(), argThat { message == "handler1: end handler2#log" })
      verify(handler).log(any(), argThat { message == "handler2: end handler2#log" })

      verifyNoMoreInteractions()
    }
  }

  @Test
  fun log_GivenManyLoggersProducingLogsWhileLogging_LogItOnlyOnceAndStopRecursion() {
    lateinit var handler1: LogHandler
    lateinit var handler2: LogHandler

    fun newLogger(): Logger = Logger("myTag", listOf(handler1, handler2))

    val handler = mock<LogHandler>()

    handler1 = object : LogHandler {
      override fun log(tag: String, logMessage: LogMessage) {
        val logger = newLogger()
        logger.debug("start handler1#log")
        handler.log(tag, logMessage.copy(message = "handler1: ${logMessage.message}"))
        logger.debug("end handler1#log")
      }
    }

    handler2 = object : LogHandler {
      override fun log(tag: String, logMessage: LogMessage) {
        val logger = newLogger()
        logger.debug("start handler2#log")
        handler.log(tag, logMessage.copy(message = "handler2: ${logMessage.message}"))
        logger.debug("end handler2#log")
      }
    }

    newLogger().log(LogMessage(message = "dummy message"))

    inOrder(handler) {
      verify(handler).log(any(), argThat { message == "handler1: start handler1#log" })
      verify(handler).log(any(), argThat { message == "handler2: start handler1#log" })
      verify(handler).log(any(), argThat { message == "handler1: dummy message" })
      verify(handler).log(any(), argThat { message == "handler1: end handler1#log" })
      verify(handler).log(any(), argThat { message == "handler2: end handler1#log" })

      verify(handler).log(any(), argThat { message == "handler1: start handler2#log" })
      verify(handler).log(any(), argThat { message == "handler2: start handler2#log" })
      verify(handler).log(any(), argThat { message == "handler2: dummy message" })
      verify(handler).log(any(), argThat { message == "handler1: end handler2#log" })
      verify(handler).log(any(), argThat { message == "handler2: end handler2#log" })

      verifyNoMoreInteractions()
    }
  }
}
