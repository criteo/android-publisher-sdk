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

import com.criteo.publisher.util.CompletableFuture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.properties.Delegates.notNull

class ThreadingUtilTest {

  @Test
  fun getUninterruptibly_GivenCompleteFuture_GetItWithoutInterruptingThread() {
    val future = CompletableFuture.completedFuture(42)

    val result = ThreadingUtil.getUninterruptibly(future)

    assertThat(result).isEqualTo(42)
    assertThat(Thread.currentThread().isInterrupted).isFalse()
  }

  @Test(timeout = 5000L)
  fun getUninterruptibly_GivenUncompletedFuture_WaitForItAndGetItWithoutInterruptingThread() {
    val executor = Executors.newFixedThreadPool(1)
    val future = CompletableFuture<Int>()

    val isBeforeGettingFuture = CountDownLatch(1)
    var threadWasInterrupted by notNull<Boolean>()

    val resultFuture = executor.submit(Callable<Int> {
      isBeforeGettingFuture.countDown()
      val value = ThreadingUtil.getUninterruptibly(future)
      threadWasInterrupted = Thread.currentThread().isInterrupted
      value
    })

    isBeforeGettingFuture.await()
    future.complete(42)
    val result = resultFuture.get()

    assertThat(result).isEqualTo(42)
    assertThat(threadWasInterrupted).isFalse()
  }

  @Test
  fun getUninterruptibly_GivenThrowingFuture_GetItAndRethrowException() {
    val executor = Executors.newFixedThreadPool(1)
    val exception = RuntimeException()

    val future = executor.submit(Callable<Void> {
      throw exception
    })

    assertThatCode {
      ThreadingUtil.getUninterruptibly(future)
    }.isInstanceOf(ExecutionException::class.java).hasCauseReference(exception)

    assertThat(Thread.currentThread().isInterrupted).isFalse()
  }

  @Test
  fun getUninterruptibly_GivenInterruptedThread_IgnoreInterruptionAndGetResultButMarkThreadAsBeingInterrupted() {
    val executor = Executors.newFixedThreadPool(2)

    val isExecutingTask = CountDownLatch(1)
    val hasBeenInterrupted = CountDownLatch(1)

    val future = executor.submit(Callable<Int> {
      isExecutingTask.countDown()
      hasBeenInterrupted.await()
      42
    })

    isExecutingTask.await()
    Thread.currentThread().interrupt()
    hasBeenInterrupted.countDown()
    val result = ThreadingUtil.getUninterruptibly(future)

    assertThat(result).isEqualTo(42)
    assertThat(Thread.currentThread().isInterrupted).isTrue()
  }
}
