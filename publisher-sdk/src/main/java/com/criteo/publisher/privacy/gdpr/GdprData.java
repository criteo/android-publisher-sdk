package com.criteo.publisher.privacy.gdpr;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.Util.CustomAdapterFactory;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class GdprData {

  @VisibleForTesting
  public static GdprData create(
      boolean consentGiven,
      @NonNull String consentData,
      boolean gdprApplies,
      @NonNull Integer version
  ) {
    return new AutoValue_GdprData(consentGiven, consentData, gdprApplies, version);
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
    String s = new GsonBuilder()
        .registerTypeAdapterFactory(CustomAdapterFactory.create())
        .create()
        .toJson(this);

    return new JSONObject(s);
  }

  public abstract boolean consentGiven();

  public abstract String consentData();

  public abstract boolean gdprApplies();

  public abstract Integer version();
}
