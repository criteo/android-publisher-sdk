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
