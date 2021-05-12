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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ConsoleHandlerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  private lateinit var handler: ConsoleHandler

  @Before
  fun setUp() {
    handler = spy(ConsoleHandler(buildConfigWrapper))
  }

  @Test
  fun new_GivenBuildConfigWrapper_UseDefaultMinLogLevel() {
    whenever(buildConfigWrapper.defaultMinLogLevel).doReturn(Log.DEBUG)

    handler = ConsoleHandler(buildConfigWrapper)

    assertThat(handler.minLogLevel).isEqualTo(Log.DEBUG)
  }

  @Test
  fun log_GivenLogLevelAboveMinimum_PrintMessage() {
    handler.minLogLevel = Log.VERBOSE
    handler.log("tag", LogMessage(Log.DEBUG, "foo"))

    verify(handler).println(Log.DEBUG, "tag", "foo")
  }

  @Test
  fun log_GivenMinLogLevelHigherThanLogMessage_IgnoreLog() {
    handler.minLogLevel = Log.INFO
    handler.log("tag", LogMessage(Log.DEBUG, "foo"))

    verify(handler, never()).println(any(), any(), any())
  }

  @Test
  fun log_GivenMessageAndThrowable_PrintMessageThenStacktrace() {
    val exception = Exception()
    doReturn("stacktrace").whenever(handler).getStackTraceString(exception)

    handler.minLogLevel = Log.INFO
    handler.log("tag", LogMessage(Log.INFO, "foo", exception))

    inOrder(handler) {
      verify(handler).println(Log.INFO, "tag", "foo\nstacktrace")
      verifyNoMoreInteractions()
    }
  }

  @Test
  fun log_GivenOnlyThrowable_PrintStacktrace() {
    val exception = Exception()
    doReturn("stacktrace").whenever(handler).getStackTraceString(exception)

    handler.minLogLevel = Log.WARN
    handler.log("tag", LogMessage(Log.WARN, null, exception))

    inOrder(handler) {
      verify(handler).println(Log.WARN, "tag", "stacktrace")
      verifyNoMoreInteractions()
    }
  }

  @Test
  fun log_GivenNoMessageAndNoThrowable_IgnoreLog() {
    whenever(buildConfigWrapper.defaultMinLogLevel).doReturn(Log.INFO)

    handler.log("tag", LogMessage(Log.INFO, null, null))

    verify(handler, never()).println(any(), any(), any())
  }
}
