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
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.*
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
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Test
  fun debug_GivenMessageAndArgs_PrintFormattedMessage() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.VERBOSE)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.debug("Hello %s", "World")

    verify(logger).println(Log.DEBUG, "Hello World")
  }

  @Test
  fun debug_GivenMessageAndThrowable_PrintMessageThenStacktrace() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.DEBUG)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.debug("Hello", Exception())

    inOrder(logger) {
      verify(logger).println(Log.DEBUG, "Hello")
      verify(logger).println(eq(Log.DEBUG), anyOrNull())
      verifyNoMoreInteractions()
    }
  }

  @Test
  fun debug_GivenMinLogLevelHigherThanDebug_IgnoreLog() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.INFO)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.debug("Hello")

    verify(logger, never()).println(any(), any())
  }

  @Test
  fun info_GivenMessageAndThrowable_PrintMessageThenStacktrace() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.INFO)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.info("Hello", Exception())

    inOrder(logger) {
      verify(logger).println(Log.INFO, "Hello")
      verify(logger).println(eq(Log.INFO), anyOrNull())
      verifyNoMoreInteractions()
    }
  }

  @Test
  fun info_GivenMessageAndArgs_PrintFormattedMessage() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.INFO)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.info("Hello %s", "World")

    verify(logger).println(Log.INFO, "Hello World")
  }


  @Test
  fun info_GivenMinLogLevelHigherThanInfo_IgnoreLog() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.WARN)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.info("Hello")

    verify(logger, never()).println(any(), any())
  }

  @Test
  fun error_GivenJustThrowable_PrintStacktrace() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.ERROR)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.error(Exception())

    verify(logger).println(eq(Log.ERROR), anyOrNull())
  }

  @Test
  fun error_GivenThrowableAndMessage_PrintMessageThenStacktrace() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.INFO)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.error("Hello", Exception())

    inOrder(logger) {
      verify(logger).println(Log.ERROR, "Hello")
      verify(logger).println(eq(Log.ERROR), anyOrNull())
      verifyNoMoreInteractions()
    }
  }

  @Test
  fun error_GivenMinLogLevelHigherThanError_IgnoreLog() {
    whenever(buildConfigWrapper.minLogLevel).doReturn(Log.ASSERT)
    val logger = spy(Logger(javaClass, buildConfigWrapper))

    logger.error(Exception())

    verify(logger, never()).println(any(), any())
  }

}