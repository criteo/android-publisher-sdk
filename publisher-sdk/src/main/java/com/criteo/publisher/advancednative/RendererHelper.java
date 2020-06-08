package com.criteo.publisher.advancednative;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import java.net.URL;

@Keep
public class RendererHelper {

  @NonNull
  private final ImageLoaderHolder imageLoaderHolder;

  @NonNull
  private final RunOnUiThreadExecutor uiExecutor;

  public RendererHelper(
      @NonNull ImageLoaderHolder imageLoaderHolder,
      @NonNull RunOnUiThreadExecutor uiExecutor
  ) {
    this.imageLoaderHolder = imageLoaderHolder;
    this.uiExecutor = uiExecutor;
  }

  void preloadMedia(@NonNull URL url) {
    new SafeRunnable() {
      @Override
      public void runSafely() throws Throwable {
        imageLoaderHolder.get().preload(url);
      }
    }.run();
  }

  public void setMediaInView(CriteoMedia mediaContent, CriteoMediaView mediaView) {
    setMediaInView(mediaContent.getImageUrl(), mediaView.getImageView(), mediaView.getPlaceholder());
  }

  void setMediaInView(@NonNull URL url, @NonNull ImageView imageView, @Nullable Drawable placeholder) {
    uiExecutor.execute(new SafeRunnable() {
      @Override
      public void runSafely() throws Throwable {
        imageLoaderHolder.get().loadImageInto(url, imageView, placeholder);
      }
    });
  }

}
