package com.criteo.publisher.Util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.util.concurrent.Executor;

public class RunOnUiThreadExecutor implements Executor {

  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void execute(@NonNull Runnable command) {
    if (Thread.currentThread() == handler.getLooper().getThread()) {
      command.run();
    } else {
      handler.post(command);
    }
  }
}
