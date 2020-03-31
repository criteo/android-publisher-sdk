package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.Util.PreconditionsUtil;
import com.criteo.publisher.network.PubSdkApi;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executor;

class MetricSendingQueueConsumer {

  @NonNull
  private final MetricSendingQueue queue;

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final Executor executor;

  MetricSendingQueueConsumer(
      @NonNull MetricSendingQueue queue,
      @NonNull PubSdkApi api,
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull Executor executor
  ) {
    this.queue = queue;
    this.api = api;
    this.buildConfigWrapper = buildConfigWrapper;
    this.executor = executor;
  }

  /**
   * Send asynchronously a new batch of metrics to the CSM backend.
   * <p>
   * This is a fire and forget operation. No output is expected. Although, if an error occurs while
   * sending the metrics to the backend, they are pushed back in the sending queue.
   * <p>
   * The batch is polled from the queue (instead of peeked). Data loss is tolerated if the process
   * is terminated while the batch is being sent to the CSM backed. This is to ensure that the same
   * metric will never be sent to CSM backend twice.
   */
  void sendMetricBatch() {
    executor.execute(new MetricSendingTask(queue, api, buildConfigWrapper));
  }

  private static class MetricSendingTask implements Runnable {

    @NonNull
    private final MetricSendingQueue queue;

    @NonNull
    private final PubSdkApi api;

    @NonNull
    private final BuildConfigWrapper buildConfigWrapper;

    private MetricSendingTask(
        @NonNull MetricSendingQueue queue,
        @NonNull PubSdkApi api,
        @NonNull BuildConfigWrapper buildConfigWrapper
    ) {
      this.queue = queue;
      this.api = api;
      this.buildConfigWrapper = buildConfigWrapper;
    }

    @Override
    public void run() {
      try {
        doRun();
      } catch (Exception e) {
        PreconditionsUtil.throwOrLog(e);
      }
    }

    private void doRun() throws IOException {
      Collection<Metric> metrics = queue.poll(buildConfigWrapper.getCsmBatchSize());
      if (metrics.isEmpty()) {
        return;
      }

      boolean success = false;
      try {
        MetricRequest request = createRequest(metrics);
        api.postCsm(request);
        success = true;
      } finally {
        if (!success) {
          rollback(metrics);
        }
      }
    }

    private MetricRequest createRequest(Collection<Metric> metrics) {
      String sdkVersion = buildConfigWrapper.getSdkVersion();
      int profileId = buildConfigWrapper.getProfileId();
      return MetricRequest.create(metrics, sdkVersion, profileId);
    }

    private void rollback(Collection<Metric> metrics) {
      for (Metric metric : metrics) {
        queue.offer(metric);
      }
    }
  }

}
