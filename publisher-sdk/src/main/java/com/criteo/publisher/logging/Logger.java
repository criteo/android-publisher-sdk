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

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

public class Logger {

  @NonNull
  private final String tag;

  @NonNull
  private final ConsoleHandler consoleHandler;

  public Logger(
      @NonNull Class<?> klass,
      @NonNull ConsoleHandler consoleHandler
  ) {
    this(klass.getSimpleName(), consoleHandler);
  }

  @VisibleForTesting
  Logger(
      @NonNull String tag,
      @NonNull ConsoleHandler consoleHandler
  ) {
    this.tag = tag;
    this.consoleHandler = consoleHandler;
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
    consoleHandler.log(tag, logMessage);
  }

  private LogMessage simpleLogMessage(int level, @Nullable String message, @Nullable Throwable throwable) {
    return new LogMessage(level, message, throwable);
  }

  private LogMessage formattedLogMessage(int level, @NonNull String message, Object[] args) {
    return new LogMessage(level, String.format(message, args), null);
  }

}
