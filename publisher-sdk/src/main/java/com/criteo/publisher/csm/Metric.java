package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;

@AutoValue
abstract class Metric {

  @NonNull
  static Metric.Builder builder() {
    return new AutoValue_Metric.Builder()
        .setReadyToSend(false);
  }

  @Nullable
  abstract Long getCdbCallStartTimestamp();

  @Nullable
  abstract Long getCdbCallEndTimestamp();

  @Nullable
  abstract Long getCdbCallTimeoutTimestamp();

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
    abstract Builder setCdbCallTimeoutTimestamp(Long absoluteTimeInMillis);
    abstract Builder setElapsedTimestamp(Long absoluteTimeInMillis);
    abstract Builder setImpressionId(String impressionId);
    abstract Builder setReadyToSend(boolean isReadyToSend);
    abstract Metric build();
  }

}
