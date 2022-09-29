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

package com.criteo.publisher.model;

import static com.criteo.publisher.model.DeviceInfoLogMessage.onErrorDuringWebViewUserAgentGet;

import android.content.Context;
import android.webkit.WebSettings;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.util.CompletableFuture;
import com.criteo.publisher.util.PreconditionsUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceInfo {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final Context context;

  @NonNull
  private final Executor executor;

  @NonNull
  private final CompletableFuture<String> userAgentFuture = new CompletableFuture<>();

  @NonNull
  private final AtomicBoolean isInitialized = new AtomicBoolean(false);

  public DeviceInfo(@NonNull Context context, @NonNull Executor executor) {
    this.context = context;
    this.executor = executor;
  }

  public void initialize() {
    if (isInitialized.get()) {
      return;
    }

    runSafely(() -> {
      if (isInitialized.compareAndSet(false, true)) {
        String userAgent = resolveUserAgent();
        userAgentFuture.complete(userAgent);
      }
    });
  }

  @NonNull
  public Future<String> getUserAgent() {
    // Initialize automatically so that it's safe to call this method alone.
    initialize();

    return userAgentFuture;
  }

  private void runSafely(Runnable runnable) {
    Runnable safeRunnable = new SafeRunnable() {
      @Override
      public void runSafely() {
        runnable.run();
      }
    };

    executor.execute(safeRunnable);
  }

  @VisibleForTesting
  @NonNull
  @WorkerThread
  String resolveUserAgent() {
    try {
      return getWebViewUserAgent();
    } catch (Throwable t) {
      logger.log(onErrorDuringWebViewUserAgentGet(t));
      return getDefaultUserAgent();
    }
  }

  @WorkerThread
  private String getWebViewUserAgent() {
    return WebSettings.getDefaultUserAgent(context);
  }

  @NonNull
  private String getDefaultUserAgent() {
    String userAgent = null;

    try {
      // There is no SecurityException on Android, so normally this safe
      userAgent = System.getProperty("http.agent");
    } catch (Throwable tr) {
      PreconditionsUtil.throwOrLog(tr);
    }

    return userAgent != null ? userAgent : "";
  }
}