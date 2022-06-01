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

package com.criteo.publisher.config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class RemoteConfigRequest {

  @NonNull
  public static RemoteConfigRequest create(
      @NonNull String criteoPublisherId,
      @NonNull String bundleId,
      @NonNull String sdkVersion,
      int profileId,
      @Nullable String deviceId
  ) {
    return new AutoValue_RemoteConfigRequest(criteoPublisherId, bundleId, sdkVersion, profileId, deviceId, "android");
  }

  public static TypeAdapter<RemoteConfigRequest> typeAdapter(Gson gson) {
    return new AutoValue_RemoteConfigRequest.GsonTypeAdapter(gson);
  }

  @NonNull
  @SerializedName("cpId")
  public abstract String getCriteoPublisherId();

  @NonNull
  public abstract String getBundleId();

  @NonNull
  public abstract String getSdkVersion();

  @SerializedName("rtbProfileId")
  public abstract int getProfileId();

  /**
   * Field used by the remote config to A/B test some configurations.
   */
  @Nullable
  public abstract String getDeviceId();

  @NonNull
  public abstract String getDeviceOs();

}
