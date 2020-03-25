package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.CustomAdapterFactory;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    String s = new GsonBuilder()
        .registerTypeAdapterFactory(CustomAdapterFactory.create())
        .create()
        .toJson(this);

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
