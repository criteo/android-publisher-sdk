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
import android.util.Log;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.util.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceInfo {

  private static final String TAG = DeviceInfo.class.getSimpleName();

  private final Context context;
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;
  private final CompletableFuture<String> userAgentFuture = new CompletableFuture<>();
  private final AtomicBoolean isInitialized = new AtomicBoolean(false);

  public DeviceInfo(Context context, RunOnUiThreadExecutor runOnUiThreadExecutor) {
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
    Runnable safeRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
        } catch (Throwable tr) {
          Log.e(TAG, "Internal error while setting user-agent.", tr);
        }
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
  private static String getDefaultUserAgent() {
    String userAgent = null;

    try {
      userAgent = System.getProperty("http.agent");
    } catch (Throwable tr) {
      Log.e(TAG, "Unable to retrieve system user-agent.", tr);
    }

    return userAgent != null ? userAgent : "";
  }
}