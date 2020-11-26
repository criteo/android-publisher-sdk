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
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TrackingCommandsExecutorTest {

  @Test(timeout = 1000)
  fun waitCommands_GivenAsyncTask_WaitForIt() {
    val executor = newSingleThreadExecutor()
    val testAndTackAreReady = CyclicBarrier(2)
    val waitCommandsFinished = CountDownLatch(1)
    var waitCommandsDidWaitForTask = false

    executor.execute {
      testAndTackAreReady.await()
      waitCommandsDidWaitForTask = !waitCommandsFinished.await(500, TimeUnit.MILLISECONDS)
    }

    testAndTackAreReady.await()
    executor.waitCommands()
    assertThat(waitCommandsDidWaitForTask).isTrue()
  }

  @Test(timeout = 1000)
  fun waitCommands_GivenAsyncTaskThrowing_StopWaiting() {
    val executor = newSingleThreadExecutor()
    val testAndTackAreReady = CyclicBarrier(2)

    executor.execute {
      testAndTackAreReady.await()
      throw Exception()
    }

    testAndTackAreReady.await()
    executor.waitCommands()
  }

  private fun newSingleThreadExecutor(): TrackingCommandsExecutor {
    return TrackingCommandsExecutor(Executors.newSingleThreadExecutor())
  }
}
