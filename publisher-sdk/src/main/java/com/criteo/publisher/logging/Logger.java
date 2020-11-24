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

package com.criteo.publisher.logging;

import static com.criteo.publisher.logging.ConsoleHandler.TagPrefix;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.List;

public class Logger {

  private static final String FALLBACK_TAG = TagPrefix + "Logger";

  @NonNull
  private final String tag;

  @NonNull
  private final List<LogHandler> handlers;

  public Logger(
      @NonNull Class<?> klass,
      @NonNull List<LogHandler> handlers
  ) {
    this(klass.getSimpleName(), handlers);
  }

  @VisibleForTesting
  Logger(
      @NonNull String tag,
      @NonNull List<LogHandler> handlers
  ) {
    this.tag = tag;
    this.handlers = handlers;
  }

  public void debug(Throwable thrown) {
    log(simpleLogMessage(Log.DEBUG, null, thrown));
  }

  public void debug(String message, Throwable thrown) {
    log(simpleLogMessage(Log.DEBUG, message, thrown));
  }

  public void debug(String message, Object... args) {
    log(formattedLogMessage(Log.DEBUG, message, args));
  }

  public void info(String message, Throwable thrown) {
    log(simpleLogMessage(Log.INFO, message, thrown));
  }

  public void info(String message, Object... args) {
    log(formattedLogMessage(Log.INFO, message, args));
  }

  public void warning(String message, Object... args) {
    log(formattedLogMessage(Log.WARN, message, args));
  }

  public void error(Throwable thrown) {
    log(simpleLogMessage(Log.ERROR, null, thrown));
  }

  public void error(String message, Throwable thrown) {
    log(simpleLogMessage(Log.ERROR, message, thrown));
  }

  public void log(@NonNull LogMessage logMessage) {
    for (LogHandler handler : handlers) {
      try {
        handler.log(tag, logMessage);
      } catch (Exception e) {
        Log.w(FALLBACK_TAG, "Impossible to log with handler: " + handler.getClass(), e);
      }
    }
  }

  private LogMessage simpleLogMessage(int level, @Nullable String message, @Nullable Throwable throwable) {
    return new LogMessage(level, message, throwable);
  }

  private LogMessage formattedLogMessage(int level, @NonNull String message, Object[] args) {
    return new LogMessage(level, String.format(message, args), null);
  }

}
