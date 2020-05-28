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
}
