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

import com.criteo.publisher.concurrent.AsyncResources
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class MockedDependenciesRuleTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var asyncResources: AsyncResources

  @Test(timeout = 2000)
  fun waitForIdleState_GivenAsyncTasksDeclaredByAsyncResources_WaitForResourceReleasing() {
    val executor = Executors.newFixedThreadPool(2)
    val shouldReleaseResource = CountDownLatch(1)

    asyncResources.newResource {
      executor.execute {
        shouldReleaseResource.await()
        release()
      }
    }

    assertThatCode {
      executor.submit {
        mockedDependenciesRule.waitForIdleState()
      }.get(500, TimeUnit.MILLISECONDS)
    }.isInstanceOf(TimeoutException::class.java)

    shouldReleaseResource.countDown()

    mockedDependenciesRule.waitForIdleState()
  }

  @Test
  fun withMockedLogger_GivenAnyCreatedLogger_IsAMockContainsByTheMockedDependencyProvider() {
    mockedDependenciesRule.withSpiedLogger()

    // Resetting the rule because withMockedLogger should normally be called at the rule creation.
    mockedDependenciesRule.resetAllDependencies()

    val loggerFactory = mockedDependenciesRule.dependencyProvider.provideLoggerFactory()
    val logger1 = loggerFactory.createLogger(javaClass)
    val logger2 = loggerFactory.createLogger(loggerFactory.javaClass)

    assertThat(logger1).isSameAs(mockedDependenciesRule.spiedLogger)
    assertThat(logger2).isSameAs(mockedDependenciesRule.spiedLogger)
  }

  @Test
  fun withMockedLogger_WithoutTheOption_LoggerFactoryWorkNormallyAndMockedDependencyProviderContainsNoLogger() {
    // Given no withMockedLogger

    val loggerFactory = mockedDependenciesRule.dependencyProvider.provideLoggerFactory()
    val logger1 = loggerFactory.createLogger(javaClass)
    val logger2 = loggerFactory.createLogger(loggerFactory.javaClass)

    assertThat(logger1).isNotNull.isNotEqualTo(logger2)
    assertThat(logger2).isNotNull
    assertThat(mockedDependenciesRule.spiedLogger).isNull()
  }
}
