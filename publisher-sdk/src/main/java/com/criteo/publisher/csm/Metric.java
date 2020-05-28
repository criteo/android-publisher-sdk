package com.criteo.publisher.csm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Metric {

  @NonNull
  public static Metric.Builder builder(String impressionId) {
    return builder().setImpressionId(impressionId);
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

  /**
   * Uniquely identifies a slot from a CDB request.
   * <p>
   * This ID is generated on client side so we can track the life of this slot during a CDB call.
   * For CSM, this allows studies at slot level.
   */
  @NonNull
  abstract String getImpressionId();

  /**
   * Uniquely identifies a CDB bid request.
   * <p>
   * This ID is generated on client side so we can track the life of a request even after a CDB
   * call. All metrics coming from the same request should have the same request group ID. For CSM,
   * this allows studies at request level (for instance to determine the timeout rate vs the number
   * of slots in the same request).
   */
  @Nullable
  abstract String getRequestGroupId();

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
    abstract Builder setRequestGroupId(String requestGroupId);
    abstract Builder setReadyToSend(boolean isReadyToSend);
    abstract Metric build();

    /**
     * @hidden
     * @deprecated this should only be used for deserialization
     */
    @Deprecated
    abstract Builder setImpressionId(String impressionId);
  }

  /**
   * @hidden
   * @deprecated this should only be used for deserialization
   */
  @Deprecated
  @NonNull
  public static Metric.Builder builder() {
    return new AutoValue_Metric.Builder()
        .setReadyToSend(false)
        .setCdbCallTimeout(false)
        .setCachedBidUsed(false);
  }

}
