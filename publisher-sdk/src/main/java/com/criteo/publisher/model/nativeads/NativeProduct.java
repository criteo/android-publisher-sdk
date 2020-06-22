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

package com.criteo.publisher.model.nativeads;

import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import java.net.URI;
import java.net.URL;

@AutoValue
public abstract class NativeProduct {

  public static TypeAdapter<NativeProduct> typeAdapter(Gson gson) {
    return new AutoValue_NativeProduct.GsonTypeAdapter(gson);
  }

  @NonNull
  public abstract String getTitle();

  @NonNull
  public abstract String getDescription();

  @NonNull
  public abstract String getPrice();

  /**
   * This is an {@link URI} and not an {@link URL}, because deeplink are acceptable.
   */
  @NonNull
  public abstract URI getClickUrl();

  @NonNull
  public abstract String getCallToAction();

  @NonNull
  abstract NativeImage getImage();

  @NonNull
  public URL getImageUrl() {
    return getImage().getUrl();
  }

}
