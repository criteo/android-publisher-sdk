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

package com.criteo.publisher.advancednative;

import androidx.annotation.NonNull;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.network.PubSdkApi;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;

public class ImpressionHelper {

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final Executor executor;

  @NonNull
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;

  public ImpressionHelper(
      @NonNull PubSdkApi api,
      @NonNull Executor executor,
      @NonNull RunOnUiThreadExecutor runOnUiThreadExecutor) {
    this.api = api;
    this.executor = executor;
    this.runOnUiThreadExecutor = runOnUiThreadExecutor;
  }

  /**
   * Fire and forget the given pixels
   * <p>
   * Each pixel are fired in asynchronously independently. This means that if one fail, it fails
   * silently and other continues.
   *
   * @param pixels list of pixels to fire
   */
  void firePixels(@NonNull Iterable<URL> pixels) {
    for (URL impressionPixel : pixels) {
      executor.execute(new PixelTask(impressionPixel, api));
    }
  }

  /**
   * Notify the given listener for {@linkplain CriteoNativeAdListener#onAdImpression() impression}.
   * <p>
   * To allow implementation do UI job, then the listener is invoked on UI thread.
   *
   * @param listener listener to notify
   */
  void notifyImpression(@NonNull CriteoNativeAdListener listener) {
    runOnUiThreadExecutor.executeAsync(new SafeRunnable() {
      @Override
      public void runSafely() {
        listener.onAdImpression();
      }
    });
  }

  private static class PixelTask extends SafeRunnable {

    @NonNull
    private final URL impressionPixel;

    @NonNull
    private final PubSdkApi api;

    private PixelTask(
        @NonNull URL impressionPixel,
        @NonNull PubSdkApi api
    ) {
      this.impressionPixel = impressionPixel;
      this.api = api;
    }

    public void runSafely() throws IOException {
      try (InputStream ignored = api.executeRawGet(impressionPixel)) {
        // ignore response
      }
    }
  }
}
