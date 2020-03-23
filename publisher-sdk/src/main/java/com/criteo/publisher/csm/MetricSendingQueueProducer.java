package com.criteo.publisher.csm;

import android.support.annotation.NonNull;

class MetricSendingQueueProducer {

  @NonNull
  private final MetricSendingQueue queue;

  MetricSendingQueueProducer(@NonNull MetricSendingQueue queue) {
    this.queue = queue;
  }

  void pushAllReadyToSendInQueue(@NonNull MetricRepository repository) {
    repository.moveAllWith(new MetricMover() {
      /**
       * Indicate that all metric that are {@linkplain Metric#isReadyToSend() ready to
       * send} should be moved.
       * <p>
       * This needs to be confirmed there because this move runs on all metric of the
       * repository. This move is bulked because {@link Metric#getImpressionId()} is
       * nullable and so the caller couldn't know the ID of metrics to move.
       *
       * @param metric metric to determine
       * @return true if metric is ready to send
       */
      @Override
      public boolean shouldMove(@NonNull Metric metric) {
        return metric.isReadyToSend();
      }

      @Override
      public boolean offerToDestination(@NonNull Metric metric) {
        return queue.offer(metric);
      }
    });
  }

}
