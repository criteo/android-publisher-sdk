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
