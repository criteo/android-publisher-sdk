package com.criteo.publisher.Util;

import android.support.annotation.NonNull;

/**
 * This is a mock implementation of the {@link RunOnUiThreadExecutor} class.
 * <p>
 * Instead of posting given commands on the UI thread, it executes them directly on the current one.
 * This may be used to ease the testing and ignore the complexity of having asynchronous code.
 * <p>
 * Please notice that you still need to provide tests validating the integrations in an async
 * context, and especially on the UI thread.
 */
public final class DirectMockRunOnUiThreadExecutor extends RunOnUiThreadExecutor {

  @Override
  public void execute(@NonNull Runnable command) {
    command.run();
  }

  @Override
  public void executeAsync(@NonNull Runnable command) {
    command.run();
  }
}
