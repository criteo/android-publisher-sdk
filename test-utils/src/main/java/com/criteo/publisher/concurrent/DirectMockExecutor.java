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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectMockExecutor implements Executor {

  private final AtomicInteger runningInExecutorCount = new AtomicInteger(0);
  private AssertionError failedExpectation = null;

  @Override
  public void execute(Runnable command) {
    runningInExecutorCount.incrementAndGet();
    try {
      command.run();
    } finally {
      runningInExecutorCount.decrementAndGet();
    }
  }

  public void expectIsRunningInExecutor() {
    try {
      assertTrue(isRunningInExecutor());
    } catch (AssertionError e) {
      registerNewFailure(e);
    }
  }

  public void verifyExpectations() {
    if (failedExpectation != null) {
      throw failedExpectation;
    }
  }

  private boolean isRunningInExecutor() {
    return runningInExecutorCount.get() > 0;
  }

  private void registerNewFailure(AssertionError e) {
    if (failedExpectation == null) {
      failedExpectation = e;
    } else {
      failedExpectation.addSuppressed(e);
    }
  }
}
