package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.Collection;

class BoundedMetricRepository extends MetricRepository {

  @NonNull
  private final MetricRepository delegate;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  BoundedMetricRepository(
      @NonNull MetricRepository delegate,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.delegate = delegate;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @Override
  void addOrUpdateById(@NonNull String impressionId, @NonNull MetricUpdater updater) {
    if (getTotalSize() >= buildConfigWrapper.getMaxSizeOfCsmMetricsFolder()) {
      if (!contains(impressionId)) {
        return;
      }
    }

    delegate.addOrUpdateById(impressionId, updater);
  }

  @Override
  void moveById(@NonNull String impressionId, @NonNull MetricMover mover) {
    delegate.moveById(impressionId, mover);
  }

  @NonNull
  @Override
  Collection<Metric> getAllStoredMetrics() {
    return delegate.getAllStoredMetrics();
  }

  @Override
  int getTotalSize() {
    return delegate.getTotalSize();
  }

  @Override
  boolean contains(@NonNull String impressionId) {
    return delegate.contains(impressionId);
  }
}
