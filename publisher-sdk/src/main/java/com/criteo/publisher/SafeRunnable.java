package com.criteo.publisher;

import com.criteo.publisher.util.PreconditionsUtil;

public abstract class SafeRunnable implements Runnable {
  public abstract void runSafely() throws Throwable;

  @Override
  public void run() {
    try {
      runSafely();
    } catch (Throwable throwable) {
      PreconditionsUtil.throwOrLog(throwable);
    }
  }
}