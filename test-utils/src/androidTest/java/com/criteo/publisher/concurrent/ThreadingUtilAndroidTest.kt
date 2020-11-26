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

import android.os.Handler
import android.os.Looper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ThreadingUtilAndroidTest {

  @Test(timeout = 100)
  fun waitForMessageQueueToBeIdle_GivenNoJob_ReturnImmediately() {
    ThreadingUtil.waitForMessageQueueToBeIdle()

    // no assertion, the timeout is validating this test
  }

  @Test
  fun waitForMessageQueueToBeIdle_GivenJobPostedOnUiThreadBefore_WaitForIt() {
    val isSetOnUiThread = AtomicBoolean(false)
    val uiJobIsBeingExecuted = CountDownLatch(1)

    val handler = Handler(Looper.getMainLooper())
    handler.post {
      uiJobIsBeingExecuted.countDown()
      simulateUiWork()
      isSetOnUiThread.set(true)
    }

    uiJobIsBeingExecuted.await()
    ThreadingUtil.waitForMessageQueueToBeIdle()

    assertThat(isSetOnUiThread).isTrue
  }

  @Test
  fun waitForMessageQueueToBeIdle_GivenJobPostingAnotherOneOnUiThread_WaitForAllJobs() {
    val isSetInSecondJob = AtomicBoolean(false)
    val firstJobIsBeingExecuted = CountDownLatch(1)

    val handler = Handler(Looper.getMainLooper())

    handler.post {
      firstJobIsBeingExecuted.countDown()
      simulateUiWork()

      handler.post {
        simulateUiWork()
        isSetInSecondJob.set(true)
      }
    }

    firstJobIsBeingExecuted.await()
    ThreadingUtil.waitForMessageQueueToBeIdle()

    assertThat(isSetInSecondJob).isTrue
  }

  @Test
  fun waitForAllThreads_GivenWorkerJobsPostingUiJobsInCascadeFinishingByWorkerJob_WaitForAllJobs() {
    val executor = TrackingCommandsExecutor(Executors.newFixedThreadPool(10))
    val handler = Handler(Looper.getMainLooper())
    val isSetInLastJob = AtomicBoolean(false)

    fun postJobs(remainingIteration: Int = 10) {
      handler.post {
        simulateUiWork()
        executor.execute {
          simulateUiWork()

          if (remainingIteration == 0) {
            isSetInLastJob.set(true)
          } else {
            postJobs(remainingIteration - 1)
          }
        }
      }
    }

    postJobs()
    ThreadingUtil.waitForAllThreads(executor)

    assertThat(isSetInLastJob).isTrue
  }

  @Test
  fun waitForAllThreads_GivenWorkerJobsPostingUiJobsInCascadeFinishingByUiJob_WaitForAllJobs() {
    val executor = TrackingCommandsExecutor(Executors.newFixedThreadPool(10))
    val handler = Handler(Looper.getMainLooper())
    val isSetInLastJob = AtomicBoolean(false)

    fun postJobs(remainingIteration: Int = 10) {
      executor.execute {
        simulateUiWork()
        handler.post {
          simulateUiWork()

          if (remainingIteration == 0) {
            isSetInLastJob.set(true)
          } else {
            postJobs(remainingIteration - 1)
          }
        }
      }
    }

    postJobs()
    ThreadingUtil.waitForAllThreads(executor)

    assertThat(isSetInLastJob).isTrue
  }

  @Test
  fun waitForMessageQueueToBeIdle_GivenInterruptedWaitingThread_AbortWaitingByThrowingRuntimeAndSetInterruptionFlag() {
    Thread.currentThread().interrupt()

    assertThatCode {
      ThreadingUtil.waitForMessageQueueToBeIdle()
    }.hasCauseInstanceOf(InterruptedException::class.java)

    assertThat(Thread.currentThread().isInterrupted).isTrue()
  }

  /**
   * Simulate some works that may be done on UI thread.
   *
   * This shows that the [ThreadingUtil.waitForMessageQueueToBeIdle] method will wait if UI jobs
   * have work to do.
   */
  private fun simulateUiWork() {
    Thread.sleep(50)
  }
}
