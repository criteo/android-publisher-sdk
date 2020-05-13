package com.criteo.publisher.concurrent

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test

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