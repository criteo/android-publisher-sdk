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

import static java.util.Collections.singletonList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.util.AdUnitType;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.Collection;
import java.util.List;

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

}
