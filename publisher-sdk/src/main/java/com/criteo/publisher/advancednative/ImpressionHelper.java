package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.util.Log;
import com.criteo.publisher.Util.RunOnUiThreadExecutor;
import com.criteo.publisher.network.PubSdkApi;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executor;

class ImpressionHelper {

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final Executor executor;

  @NonNull
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;

  ImpressionHelper(
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
  void firePixels(@NonNull Iterable<URI> pixels) {
    for (URI impressionPixel : pixels) {
      executor.execute(new PixelTask(impressionPixel, api));
    }
  }

  /**
   * Notify the given listener for {@linkplain CriteoNativeAdListener#onAdImpressed() impression}.
   * <p>
   * To allow implementation do UI job, then the listener is invoked on UI thread.
   *
   * @param listener listener to notify
   */
  void notifyImpression(@NonNull CriteoNativeAdListener listener) {
    runOnUiThreadExecutor.executeAsync(new Runnable() {
      @Override
      public void run() {
        listener.onAdImpression();
      }
    });
  }

  private static class PixelTask implements Runnable {

    private static final String TAG = PixelTask.class.getSimpleName();

    @NonNull
    private final URI impressionPixel;

    @NonNull
    private final PubSdkApi api;

    private PixelTask(
        @NonNull URI impressionPixel,
        @NonNull PubSdkApi api) {
      this.impressionPixel = impressionPixel;
      this.api = api;
    }

    @Override
    public void run() {
      try {
        doRun();
      } catch (IOException e) {
        Log.e(TAG, "Error while firing impression pixel", e);
      }
    }

    private void doRun() throws IOException {
      URL url = impressionPixel.toURL();
      try (InputStream ignored = api.executeRawGet(url)) {
        // ignore response
      }
    }
  }
}
