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

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import com.criteo.publisher.util.BuildConfigWrapper;
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
  public boolean offer(@NonNull Metric metric) {
    synchronized (delegateLock) {
      if (getTotalSize() >= buildConfigWrapper.getMaxSizeOfCsmMetricSendingQueue()) {
        delegate.poll(1);
      }
      return delegate.offer(metric);
    }
  }

  @NonNull
  @Override
  public List<Metric> poll(int max) {
    synchronized (delegateLock) {
      return delegate.poll(max);
    }
  }

  @Override
  public int getTotalSize() {
    return delegate.getTotalSize();
  }
}
