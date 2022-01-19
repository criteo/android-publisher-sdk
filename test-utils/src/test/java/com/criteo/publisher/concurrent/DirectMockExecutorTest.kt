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

package com.criteo.publisher.concurrent

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DirectMockExecutorTest {

  @Test
  fun verifyExpectations_GivenNoExecution_DoNotThrow() {
    val executor = DirectMockExecutor()

    assertThatCode {
      executor.verifyExpectations()
    }.doesNotThrowAnyException()
  }

  @Test
  fun verifyExpectations_GivenExpectationSetInExecutorCommand_DoNotThrow() {
    val executor = DirectMockExecutor()

    executor.execute {
      executor.expectIsRunningInExecutor()
    }

    assertThatCode {
      executor.verifyExpectations()
    }.doesNotThrowAnyException()
  }

  @Test
  fun verifyExpectations_GivenExpectationOutsideExecutorCommand_ThrowException() {
    val executor = DirectMockExecutor()

    executor.expectIsRunningInExecutor()

    assertThatCode {
      executor.verifyExpectations()
    }.isNotNull()
  }

  @Test
  fun verifyExpectations_GivenManyExpectationsOutsideExecutorCommand_ThrowOneExceptionWithSuppressedOnes() {
    val executor = DirectMockExecutor()

    executor.expectIsRunningInExecutor()
    executor.execute {
      executor.expectIsRunningInExecutor()
    }
    executor.expectIsRunningInExecutor()

    assertThatCode {
      executor.verifyExpectations()
    }.isNotNull().satisfies {
      assertThat(it.suppressed).hasSize(1)
    }
  }

  @Test
  fun verifyExpectations_GivenWrongExpectationInSafeRunnableOfMockedAnswer_ThrowException() {
    val executor = DirectMockExecutor()

    val runnable = mock<Runnable> {
      doAnswer {
        executor.expectIsRunningInExecutor()
        null
      }.whenever(mock).run()
    }

    val safeRunnable = Runnable {
      try {
        runnable.run()
      } catch (_: Throwable) {
      }
    }

    safeRunnable.run()

    assertThatCode {
      executor.verifyExpectations()
    }.isNotNull()
  }
}
