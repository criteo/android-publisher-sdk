package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import java.util.List;

interface MetricSendingQueue {

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
  boolean offer(@NonNull Metric metric);

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
  List<Metric> poll(int max);

}
