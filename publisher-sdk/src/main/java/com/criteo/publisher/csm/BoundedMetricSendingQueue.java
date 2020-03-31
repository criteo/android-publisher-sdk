package com.criteo.publisher.csm;

import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import com.criteo.publisher.Util.BuildConfigWrapper;
import java.util.List;

class BoundedMetricSendingQueue extends MetricSendingQueue {

  @NonNull
  @GuardedBy("delegateLock")
  private final MetricSendingQueue delegate;

  @NonNull
  private final Object delegateLock = new Object();

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  BoundedMetricSendingQueue(
      @NonNull MetricSendingQueue delegate,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.delegate = delegate;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @Override
  boolean offer(@NonNull Metric metric) {
    synchronized (delegateLock) {
      if (getTotalSize() >= buildConfigWrapper.getMaxSizeOfCsmMetricSendingQueue()) {
        delegate.poll(1);
      }
      return delegate.offer(metric);
    }
  }

  @NonNull
  @Override
  List<Metric> poll(int max) {
    synchronized (delegateLock) {
      return delegate.poll(max);
    }
  }

  @Override
  int getTotalSize() {
    return delegate.getTotalSize();
  }
}
