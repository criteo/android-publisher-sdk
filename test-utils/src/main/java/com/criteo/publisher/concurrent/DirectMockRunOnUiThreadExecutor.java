package com.criteo.publisher.concurrent;

import androidx.annotation.NonNull;

/**
 * This is a mock implementation of the {@link RunOnUiThreadExecutor} class.
 * <p>
 * Instead of posting given commands on the UI thread, it executes them directly on the current one.
 * This may be used to ease the testing and ignore the complexity of having asynchronous code.
 * <p>
 * Please notice that you still need to provide tests validating the integrations in an async
 * context, and especially on the UI thread.
 */
public class DirectMockRunOnUiThreadExecutor extends RunOnUiThreadExecutor {

  private final DirectMockExecutor delegate = new DirectMockExecutor();

  @Override
  public void execute(@NonNull Runnable command) {
    delegate.execute(command);
  }

  @Override
  public void executeAsync(@NonNull Runnable command) {
    execute(command);
  }

  public void expectIsRunningInExecutor() {
    delegate.expectIsRunningInExecutor();
  }

  public void verifyExpectations() {
    delegate.verifyExpectations();
  }

}
