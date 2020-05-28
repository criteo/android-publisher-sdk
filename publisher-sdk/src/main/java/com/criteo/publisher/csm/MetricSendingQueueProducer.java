package com.criteo.publisher.csm;

import androidx.annotation.NonNull;

public class MetricSendingQueueProducer {

  @NonNull
  private final MetricSendingQueue queue;

  public MetricSendingQueueProducer(@NonNull MetricSendingQueue queue) {
    this.queue = queue;
  }

  void pushAllInQueue(@NonNull MetricRepository repository) {
    for (Metric metric : repository.getAllStoredMetrics()) {
      pushInQueue(repository, metric.getImpressionId());
    }
  }

  void pushInQueue(
      @NonNull MetricRepository repository,
      @NonNull String impressionId
  ) {
    repository.moveById(impressionId, new MetricMover() {
      @Override
      public boolean offerToDestination(@NonNull Metric metric) {
        return queue.offer(metric);
      }
    });
  }

}
