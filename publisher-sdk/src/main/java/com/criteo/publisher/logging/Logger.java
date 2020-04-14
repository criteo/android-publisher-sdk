package com.criteo.publisher.logging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.BuildConfig;

public class Logger {

  private static final Object[] EMPTY = new Object[0];

  @NonNull
  private final String tag;

  public Logger(@NonNull Class<?> klass) {
    this.tag = klass.getSimpleName();
  }

  public void debug(String message, Throwable thrown) {
    log(Log.DEBUG, message, EMPTY, thrown);
  }

  public void debug(String message, Object... args) {
    log(Log.DEBUG, message, args, null);
  }

  private void log(int level, String message, Object[] args, @Nullable Throwable thrown) {
    if (!isLoggable()) {
      return;
    }

    String formattedMessage = String.format(message, args);
    Log.println(level, tag, formattedMessage);

    if (thrown != null) {
      Log.println(level, tag, Log.getStackTraceString(thrown));
    }
  }

  public boolean isLoggable() {
    return BuildConfig.debugLogging;
  }

}
