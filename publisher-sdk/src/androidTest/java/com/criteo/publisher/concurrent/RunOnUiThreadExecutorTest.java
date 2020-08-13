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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.os.Looper;
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
  public void executeAsync_GivenCommandWhenOnOtherThread_ExecuteItAsyncOnMainThread()
      throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    executor.executeAsync(() -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      latch.countDown();
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void executeAsync_GivenCommandWhenOnMainThread_ExecuteItAsyncOnMainThread()
      throws Exception {
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