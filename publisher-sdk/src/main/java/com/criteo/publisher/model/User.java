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
import androidx.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class User {

  public static User create(
      @Nullable String deviceId,
      @Nullable String mopubConsent,
      @Nullable String uspIab,
      @Nullable String uspOptout
  ) {
    return new AutoValue_User(
        deviceId,
        "gaid",
        "android",
        mopubConsent,
        uspIab,
        uspOptout
    );
  }

  public static TypeAdapter<User> typeAdapter(Gson gson) {
    return new AutoValue_User.GsonTypeAdapter(gson);
  }

  /**
   * This method will be removed once {@link com.criteo.publisher.model.CdbRequest} will be migrated
   * to AutoValue or other annotation based serialization/deserialization solution.
   */
  @NonNull
  public JSONObject toJson() throws JSONException {
    String s = DependencyProvider.getInstance().provideGson().toJson(this);

    return new JSONObject(s);
  }

  @Nullable
  public abstract String deviceId();

  @NonNull
  public abstract String deviceIdType();

  @NonNull
  public abstract String deviceOs();

  @Nullable
  public abstract String mopubConsent();

  /**
   * US Privacy consent IAB format (for CCPA)
   */
  @Nullable
  public abstract String uspIab();

  /**
   * US Privacy optout in binary format (for CCPA)
   */
  @Nullable
  public abstract String uspOptout();
}
