package com.criteo.publisher.bid;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;

/**
 * Listener with callbacks invoked at different moment of a bid lifecycle.
 * <p>
 * There is no strong guarantee on the order of execution, also those callbacks may be invoked on
 * different threads.
 */
public interface BidLifecycleListener {

  /**
   * Callback invoked when a CDB call is about to start.
   *
   * @param request Request sent to CDB
   */
  void onCdbCallStarted(@NonNull CdbRequest request);

  /**
   * Callback invoked when a CDB call finished successfully with a response.
   * <p>
   * A successful call is a call where:
   * <ul>
   *   <li>Network was successful</li>
   *   <li>CDB answer a successful HTTP status code</li>
   *   <li>CDB payload was understood and valid</li>
   * </ul>
   *
   * @param request  Request that was sent to CDB
   * @param response Response coming from CDB
   */
  void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response);

  /**
   * Callback invoked when a CDB call failed.
   * <p>
   * Here, the failure means anything that does not make it a {@linkplain
   * #onCdbCallFinished(CdbRequest, CdbResponse) success}.
   *
   * @param request   Request that was sent to CDB
   * @param exception Exception representing the failure of the call
   */
  void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception);

}
