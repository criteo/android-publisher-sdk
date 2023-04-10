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

package com.criteo.publisher.adview

import android.util.Log
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.logging.Logger
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.verify

class MraidMessageHandlerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule().withSpiedLogger()

  @SpyBean
  private lateinit var logger: Logger

  @Mock
  private lateinit var listener: MraidMessageHandlerListener

  private lateinit var mraidMessageHandler: MraidMessageHandler

  @Before
  fun setUp() {
    mraidMessageHandler = MraidMessageHandler()
  }

  @Test
  fun whenLog_shouldDelegateToLoggerWithProperParams() {
    mraidMessageHandler.log("Warning", "TestMessage", "id")

    verify(logger).log(LogMessage(Log.WARN, "TestMessage", logId = "id"))
  }

  @Test
  fun whenLogWithNullLogId_shouldDelegateToLoggerWithProperParams() {
    mraidMessageHandler.log("Info", "TestMessage", null)

    verify(logger).log(LogMessage(Log.INFO, "TestMessage"))
  }

  @Test
  fun whenLogWithInvalidLogLevel_shouldPassDebugAsLogLevel() {
    mraidMessageHandler.log("Lol", "TestMessage", null)

    verify(logger).log(LogMessage(Log.DEBUG, "TestMessage"))
  }

  @Test
  fun whenOpen_givenListenerIsNull_shouldNotThrow() {
    assertThatCode { mraidMessageHandler.open("https://www.criteo.com") }
        .doesNotThrowAnyException()
  }

  @Test
  fun whenOpen_givenListener_shouldCallOnOpenOnListener() {
    val url = "https://www.criteo.com"
    mraidMessageHandler.setListener(listener)

    mraidMessageHandler.open(url)

    verify(listener).onOpen(url)
  }

  @Test
  fun whenExpand_givenListener_shouldCallOnExpandOnListener() {
    val width = 100.0
    val height = 100.0
    mraidMessageHandler.setListener(listener)

    mraidMessageHandler.expand(width, height)

    verify(listener).onExpand(width, height)
  }

  @Test
  fun whenExpand_givenListenerIsNull_shouldNotThrow() {
    assertThatCode { mraidMessageHandler.expand(100.0, 100.0) }
        .doesNotThrowAnyException()
  }

  @Test
  fun whenClose_givenListener_shouldCallOnCloseOnListener() {
    mraidMessageHandler.setListener(listener)

    mraidMessageHandler.close()

    verify(listener).onClose()
  }

  @Test
  fun whenClose_givenListenerIsNull_shouldNotThrow() {
    assertThatCode { mraidMessageHandler.close() }.doesNotThrowAnyException()
  }
}
