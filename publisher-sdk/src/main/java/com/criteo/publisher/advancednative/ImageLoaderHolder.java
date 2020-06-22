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
