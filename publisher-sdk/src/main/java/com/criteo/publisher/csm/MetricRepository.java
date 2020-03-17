package com.criteo.publisher.csm;

import android.support.annotation.NonNull;

public class MetricRepository {

  void updateById(@NonNull String impressionId, @NonNull MetricUpdater updater) {
    // TODO EE-885
  }

  interface MetricUpdater {
    void update(@NonNull Metric.Builder metricBuilder);
  }

}
