package com.criteo.publisher.privacy.gdpr;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.DependencyProvider;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class GdprData {

  @VisibleForTesting
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
