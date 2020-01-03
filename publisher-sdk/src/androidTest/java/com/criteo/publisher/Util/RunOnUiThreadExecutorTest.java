package com.criteo.publisher.Util;

import static org.junit.Assert.assertSame;

import android.os.Looper;
import com.criteo.publisher.ThreadingUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class RunOnUiThreadExecutorTest {

  private Executor executor = new RunOnUiThreadExecutor();

  @Test
  public void execute_GivenCommandWhenOnMainThread_ExecuteItSequentially() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    ThreadingUtil.runOnMainThreadAndWait(() -> {
      executor.execute(latch::countDown);

      try {
        latch.await(1, TimeUnit.SECONDS);
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

    latch.await(1, TimeUnit.SECONDS);
  }

}