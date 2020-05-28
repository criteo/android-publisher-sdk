package com.criteo.publisher.logging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import com.criteo.publisher.util.BuildConfigWrapper;

public class Logger {

  private static final Object[] EMPTY = new Object[0];

  @NonNull
  private final String tag;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public Logger(
      @NonNull Class<?> klass,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.tag = klass.getSimpleName();
    this.buildConfigWrapper = buildConfigWrapper;
  }

  public void debug(String message, Throwable thrown) {
    log(Log.DEBUG, message, EMPTY, thrown);
  }

  public void debug(String message, Object... args) {
    log(Log.DEBUG, message, args, null);
  }

  public void error(Throwable thrown) {
    log(Log.ERROR, null, EMPTY, thrown);
  }

  public void error(String message, Throwable thrown) {
    log(Log.ERROR, message, EMPTY, thrown);
  }

  private void log(int level, @Nullable String message, Object[] args, @Nullable Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }

    if (message != null) {
      String formattedMessage = String.format(message, args);
      println(level, formattedMessage);
    }

    if (thrown != null) {
      println(level, Log.getStackTraceString(thrown));
    }
  }

  @VisibleForTesting
  void println(int level, @NonNull String message) {
    Log.println(level, tag, message);
  }

  private boolean isLoggable(int level) {
    return level >= buildConfigWrapper.getMinLogLevel();
  }

}
