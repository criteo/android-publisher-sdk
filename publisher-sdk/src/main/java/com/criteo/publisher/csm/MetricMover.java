package com.criteo.publisher.csm;

import android.support.annotation.NonNull;

/**
 * Organize a move of metric from an original container to a destination one.
 */
interface MetricMover {

  /**
   * Indicate if the given metric should be drained from its original container.
   *
   * @param metric metric to determine
   * @return true if it should be drained, else false
   */
  boolean shouldMove(@NonNull Metric metric);

  /**
   * Try to push the given metric into the destination container.
   * <p>
   * At this point, the {@link #shouldMove(Metric)} method already gave its approval for draining
   * the given metric. And it is effectively extracted from its original container.
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
