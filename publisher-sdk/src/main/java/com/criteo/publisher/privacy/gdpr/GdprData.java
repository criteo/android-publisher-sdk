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

package com.criteo.publisher.privacy.gdpr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class GdprData {

  public static GdprData create(
      @NonNull String consentData,
      @Nullable Boolean gdprApplies,
      @NonNull Integer version
  ) {
    return new AutoValue_GdprData(consentData, gdprApplies, version);
  }

  public static TypeAdapter<GdprData> typeAdapter(Gson gson) {
    return new AutoValue_GdprData.GsonTypeAdapter(gson);
  }

  /**
   * This method will be removed once {@link com.criteo.publisher.model.CdbRequest} will be migrated
   * to AutoValue or other annotation based serialization/deserialization solution.
   */
  @NonNull
  public JSONObject toJSONObject() throws JSONException {
    String s = DependencyProvider.getInstance().provideGson().toJson(this);

    return new JSONObject(s);
  }

  public abstract String consentData();

  @Nullable
  public abstract Boolean gdprApplies();

  public abstract Integer version();
}
