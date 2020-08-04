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

package com.criteo.publisher.csm;

import static java.util.Collections.singletonList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@AutoValue
public abstract class MetricRequest {

  @NonNull
  static MetricRequest create(
      @NonNull Collection<Metric> metrics,
      @NonNull String sdkVersion,
      int profileId
  ) {
    List<MetricRequestFeedback> feedbacks = new ArrayList<>();
    for (Metric metric : metrics) {
      feedbacks.add(MetricRequestFeedback.create(metric));
    }

    return new AutoValue_MetricRequest(
        feedbacks,
        sdkVersion,
        profileId
    );
  }

  public static TypeAdapter<MetricRequest> typeAdapter(Gson gson) {
    return new AutoValue_MetricRequest.GsonTypeAdapter(gson);
  }

  @NonNull
  abstract List<MetricRequestFeedback> getFeedbacks();

  @SerializedName("wrapper_version")
  @NonNull
  abstract String getWrapperVersion();

  @SerializedName("profile_id")
  abstract int getProfileId();

  @AutoValue
  public abstract static class MetricRequestFeedback {

    @NonNull
    static MetricRequestFeedback create(@NonNull Metric metric) {
      List<MetricRequestSlot> slots = singletonList(MetricRequestSlot.create(
          metric.getImpressionId(),
          metric.getZoneId(),
          metric.isCachedBidUsed()
          )
      );

      Long elapsed = calculateDifferenceSafely(
          metric.getElapsedTimestamp(),
          metric.getCdbCallStartTimestamp()
      );

      Long cdbCallEndElapsed = calculateDifferenceSafely(
          metric.getCdbCallEndTimestamp(),
          metric.getCdbCallStartTimestamp()
      );

      return new AutoValue_MetricRequest_MetricRequestFeedback(
          slots,
          elapsed,
          metric.isCdbCallTimeout(),
          0L,
          cdbCallEndElapsed,
          metric.getRequestGroupId()
      );
    }

    public static TypeAdapter<MetricRequestFeedback> typeAdapter(Gson gson) {
      return new AutoValue_MetricRequest_MetricRequestFeedback.GsonTypeAdapter(gson);
    }

    @Nullable
    private static Long calculateDifferenceSafely(
        @Nullable Long leftOperand,
        @Nullable Long rightOperand
    ) {
      if (leftOperand == null || rightOperand == null) {
        return null;
      }
      return leftOperand - rightOperand;
    }

    @NonNull
    abstract List<MetricRequestSlot> getSlots();

    @Nullable
    abstract Long getElapsed();

    @SerializedName("isTimeout")
    abstract boolean isTimeout();

    abstract long getCdbCallStartElapsed();

    @Nullable
    abstract Long getCdbCallEndElapsed();

    @Nullable
    abstract String getRequestGroupId();
  }

  @AutoValue
  public abstract static class MetricRequestSlot {

    @NonNull
    static MetricRequestSlot create(
        @NonNull String impressionId,
        @Nullable Integer zoneId,
        boolean cachedBidUsed
    ) {
      return new AutoValue_MetricRequest_MetricRequestSlot(impressionId, zoneId, cachedBidUsed);
    }

    public static TypeAdapter<MetricRequestSlot> typeAdapter(Gson gson) {
      return new AutoValue_MetricRequest_MetricRequestSlot.GsonTypeAdapter(gson);
    }


    @Nullable
    abstract String getImpressionId();

    @Nullable
    abstract Integer getZoneId();

    abstract boolean getCachedBidUsed();
  }
}
