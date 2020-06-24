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
import com.criteo.publisher.privacy.gdpr.GdprData;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

@AutoValue
public abstract class CdbRequest {

  @NonNull
  public static CdbRequest create(
      @NonNull Publisher publisher,
      @NonNull User user,
      @NonNull String sdkVersion,
      int profileId,
      @Nullable GdprData gdprData,
      @NonNull List<CdbRequestSlot> slots) {
    return new AutoValue_CdbRequest(
        publisher,
        user,
        sdkVersion,
        profileId,
        gdprData,
        slots
    );
  }

  public static TypeAdapter<CdbRequest> typeAdapter(Gson gson) {
    return new AutoValue_CdbRequest.GsonTypeAdapter(gson);
  }

  @NonNull
  public abstract Publisher getPublisher();

  @NonNull
  public abstract User getUser();

  @NonNull
  public abstract String getSdkVersion();

  public abstract int getProfileId();

  @Nullable
  @SerializedName("gdprConsent")
  public abstract GdprData getGdprData();

  @NonNull
  public abstract List<CdbRequestSlot> getSlots();

}
