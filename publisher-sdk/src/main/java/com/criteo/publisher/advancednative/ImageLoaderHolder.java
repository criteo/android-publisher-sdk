package com.criteo.publisher.advancednative;

import androidx.annotation.NonNull;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Indirect reference to the image loader to use.
 *
 * The {@link ImageLoader} can be {@linkplain CriteoNativeLoader#setImageLoader(ImageLoader)
 * injected} by a publisher. So callers of image loader should not retain a direct
 * reference, instead they should always ask this holder for the image loader, as it may change
 * during runtime.
 */
public class ImageLoaderHolder {

  @NonNull
  private final AtomicReference<ImageLoader> imageLoaderRef;

  public ImageLoaderHolder(@NonNull ImageLoader defaultImageLoader) {
    this.imageLoaderRef = new AtomicReference<>(defaultImageLoader);
  }

  @NonNull
  ImageLoader get() {
    return imageLoaderRef.get();
  }

  void set(@NonNull ImageLoader imageLoader) {
    this.imageLoaderRef.set(imageLoader);
  }
}
