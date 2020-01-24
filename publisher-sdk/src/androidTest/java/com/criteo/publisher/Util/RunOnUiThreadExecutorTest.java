package com.criteo.publisher.Util;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.os.Looper;
import com.criteo.publisher.ThreadingUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class RunOnUiThreadExecutorTest {

  private RunOnUiThreadExecutor executor = new RunOnUiThreadExecutor();

  @Test
  public void execute_GivenCommandWhenOnMainThread_ExecuteItSequentially() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    ThreadingUtil.runOnMainThreadAndWait(() -> {
      executor.execute(latch::countDown);

      try {
        assertTrue(latch.await(1, TimeUnit.SECONDS));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test
  public void execute_GivenCommandWhenOnOtherThread_ExecuteItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    executor.execute(() -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      latch.countDown();
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void executeAsync_GivenCommandWhenOnOtherThread_ExecuteItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    executor.executeAsync(() -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      latch.countDown();
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void executeAsync_GivenCommandWhenOnMainThread_ExecuteItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);

    ThreadingUtil.runOnMainThreadAndWait(() -> {
      executor.executeAsync(() -> {
        assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
        try {
          assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        latch2.countDown();
      });

      latch.countDown();
    });

    assertTrue(latch2.await(1, TimeUnit.SECONDS));
  }

}