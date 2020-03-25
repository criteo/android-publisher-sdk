package com.criteo.publisher.model;

import static java.util.Collections.singletonList;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.CustomAdapterFactory;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        adUnitType == AdUnitType.CRITEO_CUSTOM_NATIVE,
        adUnitType == AdUnitType.CRITEO_INTERSTITIAL,
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
  public abstract String getPlacementId();

  // isNative is not accepted by AutoValue because this generates a field called native which is a
  // reserved keyword.
  @SerializedName("isNative")
  public abstract boolean isNativeAd();

  @SerializedName("interstitial")
  public abstract boolean isInterstitial();

  @NonNull
  public abstract Collection<String> getSizes();

  @NonNull
  public JSONObject toJson() throws JSONException {
    String s = new GsonBuilder()
        .registerTypeAdapterFactory(CustomAdapterFactory.create())
        .create()
        .toJson(this);

    return new JSONObject(s);
  }

}
