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

package com.criteo.publisher.concurrent;

import static com.criteo.publisher.application.InstrumentationUtil.isRunningInInstrumentationTest;

import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.util.CompletableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ThreadingUtil {

  public static void runOnMainThreadAndWait(@NonNull Runnable runnable) {
    callOnMainThreadAndWait(() -> {
      runnable.run();
      return null;
    });
  }

  public static <T> T callOnMainThreadAndWait(@NonNull Callable<T> callable) {
    CompletableFuture<T> future = new CompletableFuture<>();

    new Handler(Looper.getMainLooper()).post(() -> {
      try {
        future.complete(callable.call());
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    });

    try {
      return getUninterruptibly(future);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Invokes {@link Future#get()} and ignore interruption of thread.
   * <p>
   * Once finished, if the thread was interrupted, the interruption flag is set back for further
   * interruption control.
   *
   * @param future future to get the value from
   * @return value of the future
   * @throws ExecutionException if the future throw an exception during its execution
   */
  public static <T> T getUninterruptibly(Future<T> future) throws ExecutionException {
    boolean wasInterrupted = false;
    try {
      while (true) {
        try {
          return future.get();
        } catch (InterruptedException e) {
          wasInterrupted = true;
        }
      }
    } finally {
      if (wasInterrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Wait for all threads in the given executor and for all task on the UI thread too.
   * <p>
   * This method is also working when called on a Java VM (outside Android then), and as there is no
   * concept of UI thread, only commands in given executor are waited for.
   * <p>
   * If the current thread get interrupted while waiting, then this method ends earlier by throwing
   * a runtime exception instead of an {@link InterruptedException} just to be convenient for
   * callers. Although, the interruption flag on this thread is set on.
   *
   * @param trackingCommandsExecutor executor to wait commands from
   */
  @RequiresApi(api = VERSION_CODES.M)
  public static void waitForAllThreads(@NonNull TrackingCommandsExecutor trackingCommandsExecutor) {
    boolean newUiCommandMightHaveBeenPosted;

    do {
      // At this point, there might be pending UI and worker commands.

      if (isRunningInInstrumentationTest()) {
        waitForMessageQueueToBeIdle();
      }

      // At this point, there might be pending worker commands.

      try {
        // If we had to wait for a command, this means that a new UI commands might have been posted.
        newUiCommandMightHaveBeenPosted = trackingCommandsExecutor.waitCommands();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }

      // At this point, there might be pending UI commands if newUiCommandMightHaveBeenPosted is true.
      // Then we loop again and restart waiting for both UI and worker commands.
      // If false, then no commands should be pending. We can stop looping.

    } while (newUiCommandMightHaveBeenPosted);
  }

  /**
   * Blocks the current threads until the {@link MessageQueue} associated to the provided {@link
   * Looper} is idle.
   * <p>
   * If the current thread get interrupted while waiting, then this method ends earlier by throwing
   * a runtime exception instead of an {@link InterruptedException} just to be convenient for
   * callers. Although, the interruption flag on this thread is set on.
   */
  @RequiresApi(api = VERSION_CODES.M)
  public static void waitForMessageQueueToBeIdle() {
    CountDownLatch latch = new CountDownLatch(1);

    Looper looper = Looper.getMainLooper();
    MessageQueue messageQueue = looper.getQueue();

    messageQueue.addIdleHandler(() -> {
      latch.countDown();
      return false;
    });

    new Handler(looper).post(() -> {
      // No-op task just to give something to do to the main looper.
      // If it is doing nothing when this method is called, the idle handler may never be called
      // and then block the execution of the program.
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}
