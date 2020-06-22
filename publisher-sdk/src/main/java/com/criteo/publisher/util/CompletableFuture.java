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

package com.criteo.publisher.util;

import androidx.annotation.NonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Partial very light implementation of {@link java.util.concurrent.CompletableFuture}.
 * <p>
 * We need our own implementation because {@link java.util.concurrent.CompletableFuture} are not
 * available for Android < 24, and they are not added in the compat libraries. We could add an
 * unofficial compat library, but we should first fix EE-589.
 * <p>
 * See <a href="https://stackoverflow.com/a/38375991">streamsupport-cfuture</a>
 */
public class CompletableFuture<T> implements Future<T> {

  private final AtomicReference<Result<T>> valueRef = new AtomicReference<>();
  private final CountDownLatch isDone = new CountDownLatch(1);
  private final FutureTask<T> task;

  public CompletableFuture() {
    task = new FutureTask<>(new CompletableCallable());
  }

  public static <T> CompletableFuture<T> completedFuture(T value) {
    CompletableFuture<T> future = new CompletableFuture<>();
    future.complete(value);
    return future;
  }

  public void complete(T value) {
    valueRef.compareAndSet(null, new Result<>(value));
    isDone.countDown();
  }

  public void completeExceptionally(Exception exception) {
    valueRef.compareAndSet(null, new Result<>(exception));
    isDone.countDown();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return task.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return task.isCancelled();
  }

  @Override
  public boolean isDone() {
    return task.isDone();
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    task.run();
    return task.get();
  }

  @Override
  public T get(long timeout, @NonNull TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    task.run();
    return task.get(timeout, unit);
  }

  private class CompletableCallable implements Callable<T> {

    @Override
    public T call() throws Exception {
      isDone.await();
      return valueRef.get().get();
    }
  }

  private static final class Result<T> {

    private final T value;
    private final Exception exception;

    Result(T value) {
      this.value = value;
      this.exception = null;
    }

    Result(Exception exception) {
      this.value = null;
      this.exception = exception;
    }

    T get() throws Exception {
      if (exception != null) {
        throw exception;
      }
      return value;
    }

  }
}
