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
import java.util.List;

public abstract class MetricSendingQueue {

  /**
   * Inserts the specified element into this queue if it is possible to do
   * so immediately without violating capacity restrictions.
   *
   * In case of failure, <code>false</code> is returned, else <code>true</code>. In case of success,
   * this means that the element is persisted and committed.
   *
   * @param metric metric to insert into this queue
   * @return <code>true</code> if element was successfully inserted, else <code>false</code>
   */
  abstract boolean offer(@NonNull Metric metric);

  /**
   * Retrieves and removes up to <code>max</code> elements from this queue.
   * <p>
   * If the queue size is less than <code>max</code>, then fewer elements are returned, then the
   * queue becomes empty.
   *
   * @param max max number of element to poll from the queue
   * @return at most <code>max</code> first elements of the queue
   */
  @NonNull
  abstract List<Metric> poll(int max);

  /**
   * Return the size in bytes of all metric elements stored in this queue.
   *
   * @return total size in bytes of stored metrics
   */
  abstract int getTotalSize();

}
