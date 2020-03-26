package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Metric {

  @NonNull
  public static Metric.Builder builder() {
    return new AutoValue_Metric.Builder()
        .setReadyToSend(false)
        .setCdbCallTimeout(false)
        .setCachedBidUsed(false);
  }

  public static TypeAdapter<Metric> typeAdapter(Gson gson) {
    return new AutoValue_Metric.GsonTypeAdapter(gson);
  }

  @Nullable
  abstract Long getCdbCallStartTimestamp();

  @Nullable
  abstract Long getCdbCallEndTimestamp();

  abstract boolean isCdbCallTimeout();

  abstract boolean isCachedBidUsed();

  @Nullable
  abstract Long getElapsedTimestamp();

  @Nullable
  abstract String getImpressionId();

  abstract boolean isReadyToSend();

  @NonNull
  @SuppressWarnings("NullableProblems") // AutoValue do not add @NonNull on generated method
  abstract Metric.Builder toBuilder();

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setCdbCallStartTimestamp(Long absoluteTimeInMillis);
    abstract Builder setCdbCallEndTimestamp(Long absoluteTimeInMillis);
    abstract Builder setCdbCallTimeout(boolean isTimeout);
    abstract Builder setCachedBidUsed(boolean isCachedBidUsed);
    abstract Builder setElapsedTimestamp(Long absoluteTimeInMillis);
    abstract Builder setImpressionId(String impressionId);
    abstract Builder setReadyToSend(boolean isReadyToSend);
    abstract Metric build();
  }

}
