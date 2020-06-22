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
