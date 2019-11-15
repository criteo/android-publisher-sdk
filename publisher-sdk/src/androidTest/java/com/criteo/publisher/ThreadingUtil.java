package com.criteo.publisher;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;

public class ThreadingUtil {

  public static void runOnMainThreadAndWait(Runnable runnable) {
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

  /**
   * Wait for the SDK to send request, receive response, notify listeners when, at least, the
   * network is mocked.
   */
  public static void waitForMockedBid() {
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
