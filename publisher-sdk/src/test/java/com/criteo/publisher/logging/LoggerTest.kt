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
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
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
  private lateinit var handler: ConsoleHandler

  private lateinit var logger: Logger

  @Before
  fun setUp() {
    logger = spy(Logger("myTag", handler))
  }

  @Test
  fun debug_GivenMessageAndArgs_DelegateFormattedMessage() {
    logger.debug("Hello %s", "World")

    verify(handler).log("myTag", LogMessage(Log.DEBUG, "Hello World"))
  }

  @Test
  fun debug_GivenMessageAndThrowable_DelegateMessageAndThrowable() {
    val exception = Exception()
    logger.debug("Hello", exception)

    verify(handler).log("myTag", LogMessage(Log.DEBUG, "Hello", exception))
  }

  @Test
  fun info_GivenMessageAndThrowable_DelegateMessageAndThrowable() {
    val exception = Exception()

    logger.info("Hello", exception)

    verify(handler).log("myTag", LogMessage(Log.INFO, "Hello", exception))
  }

  @Test
  fun info_GivenMessageAndArgs_PrintFormattedMessage() {
    logger.info("Hello %s", "World")

    verify(handler).log("myTag", LogMessage(Log.INFO, "Hello World"))
  }

  @Test
  fun warning_GivenMessageAndArgs_PrintFormattedMessage() {
    logger.warning("Hello %s", "World")

    verify(handler).log("myTag", LogMessage(Log.WARN, "Hello World"))
  }

  @Test
  fun error_GivenJustThrowable_DelegateOnlyThrowable() {
    val exception = Exception()

    logger.error(exception)

    verify(handler).log("myTag", LogMessage(Log.ERROR, null, exception))
  }

  @Test
  fun error_GivenThrowableAndMessage_PrintMessageThenStacktrace() {
    val exception = Exception()

    logger.error("Hello", exception)

    verify(handler).log("myTag", LogMessage(Log.ERROR, "Hello", exception))
  }
}
