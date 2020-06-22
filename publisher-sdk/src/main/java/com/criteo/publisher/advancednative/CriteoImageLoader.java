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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.squareup.picasso.Picasso;
import java.net.URL;

public class CriteoImageLoader implements ImageLoader {

  private final Picasso picasso;

  public CriteoImageLoader(@NonNull Picasso picasso) {
    this.picasso = picasso;
  }

  @Override
  public void preload(@NonNull URL imageUrl) {
    picasso.load(imageUrl.toString()).fetch();
  }

  @UiThread
  @Override
  public void loadImageInto(
      @NonNull URL imageUrl,
      @NonNull ImageView imageView,
      @Nullable Drawable placeholder
  ) {
    picasso.load(imageUrl.toString()).placeholder(placeholder).into(imageView);
  }
}