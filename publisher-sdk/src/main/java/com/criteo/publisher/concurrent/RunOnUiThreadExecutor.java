package com.criteo.publisher.concurrent;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.util.concurrent.Executor;

public class RunOnUiThreadExecutor implements Executor {

  private final Handler handler = new Handler(Looper.getMainLooper());

  /**
   * Execute given command on the UI thread as soon as possible.
   * <p>
   * If the current thread is already the UI one, then the given command is executed directly. Else,
   * it is posted on UI thread for later execution.
   *
   * @param command to execute.
   */
  @Override
  public void execute(@NonNull Runnable command) {
    if (Thread.currentThread() == handler.getLooper().getThread()) {
      command.run();
    } else {
      handler.post(command);
    }
  }

  /**
   * Execute the given command asynchronously on the UI thread.
   * <p>
   * In all cases, even if the current thread is the UI one, the given command is posted for later
   * execution.
   * <p>
   * This may be useful in case of not controlled command to safely execute under locking
   * conditions. For instance:
   * <pre><code>
   *   private Lock _lock = new NonReentrantLock();
   *
   *   public void foo() {
   *     _lock.lock();
   *     new RunOnUiThreadExecutor().executeAsync(this::bar);
   *     _lock.unlock();
   *   }
   *
   *   public void bar() {
   *     _lock.lock();
   *     // ... do stuff
   *     _lock.unlock();
   *   }
   * </code></pre>
   *
   * @param command to execute.
   */
  public void executeAsync(@NonNull Runnable command) {
    handler.post(command);
  }
}
