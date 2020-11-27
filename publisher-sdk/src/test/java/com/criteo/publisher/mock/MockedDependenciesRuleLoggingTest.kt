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

package com.criteo.publisher.mock

import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.logging.ConsoleHandler
import com.criteo.publisher.logging.LogMessage
import com.criteo.publisher.logging.Logger
import com.criteo.publisher.logging.LoggerFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class MockedDependenciesRuleLoggingTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = object : MockedDependenciesRule() {
    override fun createDependencyProvider() = TestedDependencyProvider()
  }.withSpiedLogger()

  @SpyBean
  private lateinit var beansWithLogger: BeansWithLogger

  @SpyBean
  private lateinit var consoleHandler: ConsoleHandler

  @SpyBean
  private lateinit var logger: Logger

  @Test
  fun withSpiedLogger_InjectSpiedLoggerAndBeansProperly() {
    assertThat(beansWithLogger.logger).isNotNull.isSameAs(logger)
    assertThat(consoleHandler).isSpy()
    assertThat(beansWithLogger).isSpy()
  }

  @Test
  fun withSpiedLogger_WhenLogging_DelegateToInjectedConsoleHandler() {
    val logMessage = LogMessage(message = "dummy")

    logger.log(logMessage)

    verify(consoleHandler).log(any(), eq(logMessage))
  }

  private fun <T> ObjectAssert<T>.isSpy(): ObjectAssert<T> {
    return matches {
      Mockito.mockingDetails(it).isSpy
    }
  }

  @OpenForTesting
  class TestedDependencyProvider : TestDependencyProvider() {
    fun provideBeansWithLogger(): BeansWithLogger {
      return getOrCreate(BeansWithLogger::class.java, ::BeansWithLogger)
    }
  }

  @OpenForTesting
  class BeansWithLogger {
    val logger = LoggerFactory.getLogger(javaClass)
  }
}
