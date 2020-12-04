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

package com.criteo.publisher.csm

internal interface ConcurrentSendingQueue<T> {

  /**
   * Inserts the specified element into this queue if it is possible to do
   * so immediately without violating capacity restrictions.
   *
   * In case of failure, `false` is returned, else `true`. In case of success,
   * this means that the element is persisted and committed.
   *
   * @param element new element to insert into this queue
   * @return `true` if element was successfully inserted, else `false`
   */
  fun offer(element: T): Boolean

  /**
   * Retrieves and removes up to `max` elements from this queue.
   *
   * If the queue size is less than `max`, then fewer elements are returned, then the
   * queue becomes empty.
   *
   * @param max max number of element to poll from the queue
   * @return at most `max` first elements of the queue
   */
  fun poll(max: Int): List<T>

  /**
   * Return the size in bytes of all elements stored in this queue.
   *
   * @return total size in bytes of stored elements
   */
  val totalSize: Int
}
