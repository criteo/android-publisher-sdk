package com.criteo.publisher;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.NonNull;
import java.util.concurrent.CountDownLatch;

public class ThreadingUtil {

  public static void runOnMainThreadAndWait(@NonNull Runnable runnable) {
    CountDownLatch latch = new CountDownLatch(1);

    new Handler(Looper.getMainLooper()).post(() -> {
      runnable.run();
      latch.countDown();
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void waitForAllThreads(@NonNull TrackingCommandsExecutor trackingCommandsExecutor) {
    waitForMessageQueueToBeIdle();

    try {
      trackingCommandsExecutor.waitCommands();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Blocks the current threads until the {@link MessageQueue} associated to the provided
   * {@link Looper} is idle.
   */
  public static void waitForMessageQueueToBeIdle() {
    CountDownLatch latch = new CountDownLatch(1);

    Looper looper = Looper.getMainLooper();
    MessageQueue messageQueue = looper.getQueue();
    messageQueue.addIdleHandler(() -> {
      latch.countDown();
      return false;
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
