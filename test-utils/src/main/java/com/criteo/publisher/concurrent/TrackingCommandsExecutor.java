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

import androidx.annotation.NonNull;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;


public class TrackingCommandsExecutor implements Executor {

  private final Queue<CountDownLatch> commandLatches = new ConcurrentLinkedQueue<>();
  private final Executor delegate;

  public TrackingCommandsExecutor(Executor delegate) {
    this.delegate = delegate;
  }

  @Override
  public void execute(@NonNull Runnable command) {
    CountDownLatch latch = new CountDownLatch(1);
    commandLatches.add(latch);

    Runnable trackedCommand = () -> {
      command.run();
      latch.countDown();
    };

    delegate.execute(trackedCommand);
  }

  public AsyncResources asAsyncResources() {
    return new TrackingAsyncResources();
  }

  /**
   * Wait for all the commands passed to the {@link Executor} to finish executing
   *
   * @throws InterruptedException
   */
  public void waitCommands() throws InterruptedException {
    CountDownLatch latch;
    while ((latch = commandLatches.poll()) != null) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        commandLatches.add(latch);
        throw e;
      }
    }
  }

  private class TrackingAsyncResources extends AsyncResources {

    // Keep a local copy of latches to release. The order is not important, but this async resources
    // should only release latches coming from this #onNewAsyncResource call.
    private final Queue<CountDownLatch> resourcesLatches = new ConcurrentLinkedQueue<>();

    @Override
    protected void onNewAsyncResource() {
      CountDownLatch latch = new CountDownLatch(1);
      resourcesLatches.add(latch);
      commandLatches.add(latch);
    }

    @Override
    protected void onReleasedAsyncResource() {
      CountDownLatch latch = resourcesLatches.poll();
      if (latch != null) {
        latch.countDown();
      }
    }
  }
}
