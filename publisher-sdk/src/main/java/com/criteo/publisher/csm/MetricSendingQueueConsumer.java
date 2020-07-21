/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.csm;

import androidx.annotation.NonNull;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.concurrent.Executor;

public class MetricSendingQueueConsumer {

  @NonNull
  private final MetricSendingQueue queue;

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final Config config;

  @NonNull
  private final Executor executor;

  public MetricSendingQueueConsumer(
      @NonNull MetricSendingQueue queue,
      @NonNull PubSdkApi api,
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull Config config,
      @NonNull Executor executor
  ) {
    this.queue = queue;
    this.api = api;
    this.buildConfigWrapper = buildConfigWrapper;
    this.config = config;
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
  public void sendMetricBatch() {
    if (config.isCsmEnabled()) {
      executor.execute(new MetricSendingTask(queue, api, buildConfigWrapper));
    }
  }

}
