package com.criteo.publisher.csm;

import androidx.annotation.NonNull;
import com.criteo.publisher.DependencyProvider.Factory;
import com.criteo.publisher.util.BuildConfigWrapper;

public class MetricSendingQueueFactory implements Factory<MetricSendingQueue> {

  @NonNull
  private final MetricObjectQueueFactory metricObjectQueueFactory;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public MetricSendingQueueFactory(
      @NonNull MetricObjectQueueFactory metricObjectQueueFactory,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.metricObjectQueueFactory = metricObjectQueueFactory;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  @Override
  public MetricSendingQueue create() {
    MetricSendingQueue tapeQueue = new TapeMetricSendingQueue(metricObjectQueueFactory);
    return new BoundedMetricSendingQueue(tapeQueue, buildConfigWrapper);
  }
}
