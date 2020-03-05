package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.util.Log;
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

  ImpressionHelper(
      @NonNull PubSdkApi api,
      @NonNull Executor executor) {
    this.api = api;
    this.executor = executor;
  }

  /**
   * Fire and forget the given pixels
   * <p>
   * Each pixels are fired in asynchronously independently. This means that if one fail, it fails
   * silently and other continues.
   *
   * @param pixels list of pixels to fire
   */
  void firePixels(Iterable<URI> pixels) {
    for (URI impressionPixel : pixels) {
      executor.execute(new PixelTask(impressionPixel, api));
    }
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
