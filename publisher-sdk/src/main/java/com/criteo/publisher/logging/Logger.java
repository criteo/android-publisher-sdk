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
import androidx.annotation.VisibleForTesting;
import java.util.List;

public class Logger {

  private static final String FALLBACK_TAG = LogTag.with("Logger");

  @NonNull
  private final String tag;

  @NonNull
  private final List<LogHandler> handlers;

  /**
   * Indicate if one handler is logging another message and prevent infinite recursive loop.
   *
   * Handlers can have 1 depth of allowed logs. This means that recursive logs after 1 depth are skipped. For instance:
   * <pre><code>
   *   handler H is logging "foo" when handling a log message
   *
   *   - an actor logs a message "bar"
   *   - logger gives "bar" to handler H
   *   - handler H handles "bar"
   *   - handler H logs "foo" (looping to logger)
   *   - logger gives "foo" to handler H
   *   - handler H handles "foo"
   *   - handler H logs "foo"
   *   - logger detects recursive loop and skips this log
   * </code></pre>
   *
   * This integer contains the depth of the current logging situation:
   * <ul>
   *   <li>0 means that nothing is being logged: logger can log</li>
   *   <li>1 means that a log is being handled: logger can log</li>
   *   <li>2 means that a handler log is being handled: logger skip the log</li>
   * </ul>
   *
   * Note that this protection is only done locally to a thread to avoid {@link StackOverflowError}. If this scenario
   * happens between threads, then something really wrong is happening. Also, it is done across all loggers, so this
   * protection is working when multiple loggers are used, and when new loggers are newly created.
   *
   * Checking for recursive logs is necessary because logging can throw an exception (for example when performing an I/O
   * operation to build a log message). Throwing an exception will result in an additional log message, which can throw
   * again... hence resulting in an infinite loop.
   */
  @NonNull
  private static final ThreadLocal<Integer> logRecursionDepth = new ThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return 0;
    }
  };

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
    log(new LogMessage(Log.DEBUG, null, thrown));
  }

  public void debug(String message, Throwable thrown) {
    log(new LogMessage(Log.DEBUG, message, thrown));
  }

  public void debug(String message, Object... args) {
    log(new LogMessage(Log.DEBUG, String.format(message, args), null));
  }

  public void log(@NonNull LogMessage logMessage) {
    int depth = logRecursionDepth.get();
    if (depth > 1) {
      return;
    }

    for (LogHandler handler : handlers) {
      logRecursionDepth.set(depth + 1);
      try {
        handler.log(tag, logMessage);
      } catch (Exception e) {
        Log.w(FALLBACK_TAG, "Impossible to log with handler: " + handler.getClass(), e);
      } finally {
        if (depth == 0) {
          // See Sonar S5164: Clean current thread state to avoid memory leaks
          logRecursionDepth.remove();
        } else {
          logRecursionDepth.set(depth);
        }
      }
    }
  }

}
