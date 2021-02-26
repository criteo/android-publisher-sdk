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

public class SendingQueueFactory<T> implements Factory<ConcurrentSendingQueue<T>> {

  @NonNull
  private final ObjectQueueFactory<T> objectQueueFactory;

  @NonNull
  private final SendingQueueConfiguration<T> sendingQueueConfiguration;

  public SendingQueueFactory(
      @NonNull ObjectQueueFactory<T> objectQueueFactory,
      @NonNull SendingQueueConfiguration<T> sendingQueueConfiguration
  ) {
    this.objectQueueFactory = objectQueueFactory;
    this.sendingQueueConfiguration = sendingQueueConfiguration;
  }

  @NonNull
  @Override
  public ConcurrentSendingQueue<T> create() {
    ConcurrentSendingQueue<T> tapeQueue = new TapeSendingQueue<>(objectQueueFactory, sendingQueueConfiguration);
    return new BoundedSendingQueue<>(tapeQueue, sendingQueueConfiguration);
  }
}
