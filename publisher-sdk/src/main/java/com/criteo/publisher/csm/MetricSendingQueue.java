package com.criteo.publisher.csm;

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
  boolean offer(Metric metric);

}
