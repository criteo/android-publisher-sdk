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
