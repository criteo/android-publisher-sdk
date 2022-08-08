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

import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

@AutoValue
public abstract class Publisher {

  @NonNull
  public static Publisher create(
      @NonNull String bundleId,
      @NonNull String criteoPublisherId,
      @NonNull Map<String, Object> ext
  ) {
    return new AutoValue_Publisher(bundleId, criteoPublisherId, ext);
  }

  public static TypeAdapter<Publisher> typeAdapter(Gson gson) {
    return new AutoValue_Publisher.GsonTypeAdapter(gson);
  }

  @NonNull
  public abstract String getBundleId();

  @NonNull
  @SerializedName("cpId")
  public abstract String getCriteoPublisherId();

  @NonNull
  public abstract Map<String, Object> getExt();

}
