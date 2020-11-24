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

package com.criteo.publisher;

import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.util.PreconditionsUtil;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public abstract class SafeRunnable implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(SafeRunnable.class);

  /**
   * This stackTrace provides contextual information for tasks executed on a separate
   * thread. Specifically, it allows keeping the stacktrace of the thread from where the task was
   * sent for execution on a different thread.
   */
  private final StackTraceElement[] stackTrace;

  public SafeRunnable() {
    this.stackTrace = Thread.currentThread().getStackTrace();
  }

  @Override
  public void run() {
    try {
      runSafely();
    } catch (Throwable throwable) {
      ExecutionException e = new ExecutionException(throwable);
      e.setStackTrace(stackTrace);

      if (throwable instanceof RuntimeException) {
        PreconditionsUtil.throwOrLog(e);
      } else if (throwable instanceof IOException) {
        // IO exceptions happen when network is slow/bad/unavailable or when disk is full/unavailable, ...
        // Those are normal and expected situations. So they are not considered as errors.
        logger.debug(e);
      } else {
        logger.error(e);
      }
    }
  }

  public abstract void runSafely() throws Throwable;
}