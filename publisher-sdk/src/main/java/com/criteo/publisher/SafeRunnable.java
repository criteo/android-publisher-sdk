package com.criteo.publisher;

import com.criteo.publisher.util.PreconditionsUtil;
import java.util.concurrent.ExecutionException;

public abstract class SafeRunnable implements Runnable {

  /**
   * This stackTrace provides contextual information for tasks executed on a separate
   * thread. Specifically, it allows keeping the stacktrace of the thread from where the task was
   * sent for execution on a different thread.
   */
  private final StackTraceElement[] stackTrace;

  public SafeRunnable() {
    this.stackTrace = Thread.currentThread().getStackTrace();
  }

  @Override
  public void run() {
    try {
      runSafely();
    } catch (Throwable throwable) {
      ExecutionException e = new ExecutionException(throwable);
      e.setStackTrace(stackTrace);
      PreconditionsUtil.throwOrLog(e);
    }
  }

  public abstract void runSafely() throws Throwable;
}