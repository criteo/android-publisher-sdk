package com.criteo.publisher.model;

import androidx.annotation.NonNull;
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
      int profileId
  ) {
    return new AutoValue_RemoteConfigRequest(criteoPublisherId, bundleId, sdkVersion, profileId);
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

}
