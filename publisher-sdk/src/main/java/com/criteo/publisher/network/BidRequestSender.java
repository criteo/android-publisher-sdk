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

package com.criteo.publisher.network;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.RemoteConfigRequest;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import com.criteo.publisher.model.RemoteConfigResponse;
import com.criteo.publisher.util.CdbCallListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class BidRequestSender {

  @NonNull
  private final CdbRequestFactory cdbRequestFactory;

  @NonNull
  private final RemoteConfigRequestFactory remoteConfigRequestFactory;

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final Executor executor;

  @NonNull
  @GuardedBy("lock")
  private final Map<CacheAdUnit, Future<?>> pendingTasks;
  private final Object pendingTasksLock = new Object();

  public BidRequestSender(
      @NonNull CdbRequestFactory cdbRequestFactory,
      @NonNull RemoteConfigRequestFactory remoteConfigRequestFactory,
      @NonNull PubSdkApi api,
      @NonNull Executor executor) {
    this.cdbRequestFactory = cdbRequestFactory;
    this.remoteConfigRequestFactory = remoteConfigRequestFactory;
    this.api = api;
    this.executor = executor;
    this.pendingTasks = new ConcurrentHashMap<>();
  }

  @VisibleForTesting
  Set<CacheAdUnit> getPendingTaskAdUnits() {
    return pendingTasks.keySet();
  }

  /**
   * Asynchronously send a remote config request and update the given config.
   * <p>
   * If no error occurs during the request, the given configuration is updated. Else, it is left
   * unchanged.
   *
   * @param configToUpdate configuration to update after request
   */
  public void sendRemoteConfigRequest(@NonNull Config configToUpdate) {
    executor.execute(new RemoteConfigCall(configToUpdate));
  }

  /**
   * Asynchronously send a bid request with the given requested ad units.
   * <p>
   * The given listener is notified before and after the call was made.
   * <p>
   * When an ad unit is sent for request, it is considered as pending until the end of its request
   * (successful or not). While an ad unit is pending, it cannot be requested again. So if in given
   * ones, some are pending, they will be ignored from the request. If all given ad units are
   * pending, then no call is done and listener is not notified.
   *
   * @param adUnits ad units to request
   * @param listener listener to notify
   */
  public void sendBidRequest(
      @NonNull List<CacheAdUnit> adUnits,
      @NonNull CdbCallListener listener) {
    List<CacheAdUnit> requestedAdUnits = new ArrayList<>(adUnits);
    FutureTask<Void> task;

    synchronized (pendingTasksLock) {
      requestedAdUnits.removeAll(pendingTasks.keySet());
      if (requestedAdUnits.isEmpty()) {
        return;
      }

      task = createCdbCallTask(requestedAdUnits, listener);

      for (CacheAdUnit requestedAdUnit : requestedAdUnits) {
        pendingTasks.put(requestedAdUnit, task);
      }
    }

    try {
      executor.execute(task);
      task = null;
    } finally {
      if (task != null) {
        // If an exception was thrown when scheduling the task, then we remove the ad unit from the
        // pending tasks.
        removePendingTasksWithAdUnits(requestedAdUnits);
      }
    }
  }

  @NonNull
  private FutureTask<Void> createCdbCallTask(
      @NonNull List<CacheAdUnit> requestedAdUnits,
      @NonNull CdbCallListener listener) {
    CdbCall task = new CdbCall(requestedAdUnits, listener);

    Runnable withRemovedPendingTasksAfterExecution = new Runnable() {
      @Override
      public void run() {
        try {
          task.run();
        } finally {
          removePendingTasksWithAdUnits(requestedAdUnits);
        }
      }
    };

    return new FutureTask<>(withRemovedPendingTasksAfterExecution, null);
  }

  private void removePendingTasksWithAdUnits(List<CacheAdUnit> adUnits) {
    synchronized (pendingTasksLock) {
      pendingTasks.keySet().removeAll(adUnits);
    }
  }

  /**
   * Attempt to cancel all pending tasks of bid request.
   */
  public void cancelAllPendingTasks() {
    synchronized (pendingTasksLock) {
      for (Future<?> task : pendingTasks.values()) {
        task.cancel(true);
      }
      pendingTasks.clear();
    }
  }

  private class CdbCall extends SafeRunnable {

    @NonNull
    private final List<CacheAdUnit> requestedAdUnits;

    @NonNull
    private final CdbCallListener listener;

    private CdbCall(
        @NonNull List<CacheAdUnit> requestedAdUnits,
        @NonNull CdbCallListener listener) {
      this.requestedAdUnits = requestedAdUnits;
      this.listener = listener;
    }

    @Override
    public void runSafely() throws ExecutionException, InterruptedException {
      CdbRequest cdbRequest = cdbRequestFactory.createRequest(requestedAdUnits);
      String userAgent = cdbRequestFactory.getUserAgent().get();

      listener.onCdbRequest(cdbRequest);

      try {
        CdbResponse cdbResponse = api.loadCdb(cdbRequest, userAgent);
        listener.onCdbResponse(cdbRequest, cdbResponse);
      } catch (Exception e) {
        listener.onCdbError(cdbRequest, e);
      }
    }
  }

  private class RemoteConfigCall extends SafeRunnable {
    @NonNull
    private final Config configToUpdate;

    private RemoteConfigCall(@NonNull Config configToUpdate) {
      this.configToUpdate = configToUpdate;
    }

    @Override
    public void runSafely() throws IOException {
      RemoteConfigRequest request = remoteConfigRequestFactory.createRequest();
      RemoteConfigResponse response = api.loadConfig(request);
      configToUpdate.refreshConfig(response);
    }
  }
}
