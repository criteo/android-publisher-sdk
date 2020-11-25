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

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.util.CompletableFuture;
import com.criteo.publisher.util.PreconditionsUtil;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceInfo {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final Context context;

  @NonNull
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;

  @NonNull
  private final CompletableFuture<String> userAgentFuture = new CompletableFuture<>();

  @NonNull
  private final AtomicBoolean isInitialized = new AtomicBoolean(false);

  public DeviceInfo(@NonNull Context context, @NonNull RunOnUiThreadExecutor runOnUiThreadExecutor) {
    this.context = context;
    this.runOnUiThreadExecutor = runOnUiThreadExecutor;
  }

  public void initialize() {
    // This needs to be run on UI thread because a WebView is used to fetch the user-agent
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (isInitialized.compareAndSet(false, true)) {
          String userAgent = resolveUserAgent();
          userAgentFuture.complete(userAgent);
        }
      }
    });
  }

  @NonNull
  public Future<String> getUserAgent() {
    // Initialize automatically so that it's safe to call this method alone.
    initialize();

    return userAgentFuture;
  }

  private void runOnUiThread(Runnable runnable) {
    Runnable safeRunnable = new SafeRunnable() {
      @Override
      public void runSafely() {
        runnable.run();
      }
    };

    runOnUiThreadExecutor.executeAsync(safeRunnable);
  }

  @VisibleForTesting
  @NonNull
  @UiThread
  String resolveUserAgent() {
    String userAgent = null;

    // Try to fetch the UA from a web view
    // This may fail with a RuntimeException that is safe to ignore
    try {
      userAgent = getWebViewUserAgent();
    } catch (Throwable ignore) {
      // FIXME this is not a RuntimeException, this is a throwable that should not be
      // caught and ignored so easily.
    }

    // If we failed to get a WebView UA, try to fall back to a system UA, instead
    if (TextUtils.isEmpty(userAgent)) {
      userAgent = getDefaultUserAgent();
    }

    return userAgent;
  }

  @UiThread
  private String getWebViewUserAgent() {
    WebView webView = new WebView(context);
    String userAgent = webView.getSettings().getUserAgentString();
    webView.destroy();
    return userAgent;
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