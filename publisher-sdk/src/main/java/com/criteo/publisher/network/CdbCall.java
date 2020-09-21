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

import androidx.annotation.NonNull;
import com.criteo.publisher.CdbCallListener;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.CdbResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;

class CdbCall extends SafeRunnable {

  @NonNull
  private final PubSdkApi pubSdkApi;

  @NonNull
  private final CdbRequestFactory cdbRequestFactory;

  @NonNull
  private final List<CacheAdUnit> requestedAdUnits;

  @NonNull
  private final CdbCallListener listener;

  CdbCall(
      @NonNull PubSdkApi pubSdkApi,
      @NonNull CdbRequestFactory cdbRequestFactory,
      @NonNull List<CacheAdUnit> requestedAdUnits,
      @NonNull CdbCallListener listener
  ) {
    this.pubSdkApi = pubSdkApi;
    this.cdbRequestFactory = cdbRequestFactory;
    this.requestedAdUnits = requestedAdUnits;
    this.listener = listener;
  }

  @Override
  public void runSafely() throws ExecutionException, InterruptedException {
    CdbRequest cdbRequest = cdbRequestFactory.createRequest(requestedAdUnits);
    String userAgent = cdbRequestFactory.getUserAgent().get();

    listener.onCdbRequest(cdbRequest);

    try {
      CdbResponse cdbResponse = pubSdkApi.loadCdb(cdbRequest, userAgent);
      listener.onCdbResponse(cdbRequest, cdbResponse);
    } catch (Exception e) {
      listener.onCdbError(cdbRequest, e);
    }
  }
}
