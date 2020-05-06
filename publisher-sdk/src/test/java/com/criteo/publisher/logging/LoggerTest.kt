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