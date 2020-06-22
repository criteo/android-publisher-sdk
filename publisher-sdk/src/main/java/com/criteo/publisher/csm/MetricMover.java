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

/**
 * Organize a move of metric from an original container to a destination one.
 */
interface MetricMover {

  /**
   * Try to push the given metric into the destination container.
   * <p>
   * At this point, the given metric is effectively extracted from its original container.
   * <p>
   * Implementation should commit the move to the destination container. If the move is a success,
   * then <code>true</code> should be returned. Else <code>false</code> is returned to indicate a
   * failure.
   * <p>
   * In case of any failure (by <code>false</code> or by exception), the caller may try to insert
   * back the element into the original container in order to rollback the operation.
   *
   * @param metric metric to inject into the destination container
   * @return <code>true</code> if it is successfully committed, else <code>false</code>
   */
  boolean offerToDestination(@NonNull Metric metric);

}
