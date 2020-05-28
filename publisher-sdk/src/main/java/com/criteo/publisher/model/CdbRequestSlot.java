package com.criteo.publisher.model;

import static java.util.Collections.singletonList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.util.AdUnitType;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class CdbRequestSlot {

  public static CdbRequestSlot create(
      @NonNull String impressionId,
      @NonNull String placementId,
      @NonNull AdUnitType adUnitType,
      @NonNull AdSize size
  ) {
    List<String> formattedSizes = singletonList(size.getFormattedSize());

    return new AutoValue_CdbRequestSlot(
        impressionId,
        placementId,
        adUnitType == AdUnitType.CRITEO_CUSTOM_NATIVE ? true : null,
        adUnitType == AdUnitType.CRITEO_INTERSTITIAL ? true : null,
        formattedSizes
    );
  }

  public static TypeAdapter<CdbRequestSlot> typeAdapter(Gson gson) {
    return new AutoValue_CdbRequestSlot.GsonTypeAdapter(gson);
  }

  @NonNull
  @SerializedName("impId")
  public abstract String getImpressionId();

  @NonNull
  @SerializedName("placementId")
  public abstract String getPlacementId();

  // isNative is not accepted by AutoValue because this generates a field called native which is a
  // reserved keyword.
  @Nullable
  @SerializedName("isNative")
  public abstract Boolean isNativeAd();

  @Nullable
  @SerializedName("interstitial")
  public abstract Boolean isInterstitial();

  @NonNull
  @SerializedName("sizes")
  public abstract Collection<String> getSizes();

  @NonNull
  public JSONObject toJson() throws JSONException {
    String s = DependencyProvider.getInstance().provideGson().toJson(this);

    return new JSONObject(s);
  }

}
