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

package com.criteo.publisher.bid;

import androidx.annotation.NonNull;
import com.criteo.publisher.dependency.SdkInput;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.CdbResponseSlot;
import java.util.ArrayList;
import java.util.List;

public class CompositeBidLifecycleListener implements BidLifecycleListener {

  @NonNull
  private final List<BidLifecycleListener> delegates = new ArrayList<>();

  public void add(@NonNull BidLifecycleListener delegate) {
    delegates.add(delegate);
  }

  @Override
  public void onSdkInitialized(@NonNull SdkInput sdkInput) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onSdkInitialized(sdkInput);
    }
  }

  @Override
  public void onCdbCallStarted(@NonNull CdbRequest request) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onCdbCallStarted(request);
    }
  }

  @Override
  public void onCdbCallFinished(@NonNull CdbRequest request, @NonNull CdbResponse response) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onCdbCallFinished(request, response);
    }
  }

  @Override
  public void onCdbCallFailed(@NonNull CdbRequest request, @NonNull Exception exception) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onCdbCallFailed(request, exception);
    }
  }

  @Override
  public void onBidConsumed(@NonNull CacheAdUnit adUnit, @NonNull CdbResponseSlot consumedBid) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onBidConsumed(adUnit, consumedBid);
    }
  }

  @Override
  public void onBidCached(@NonNull CdbResponseSlot cachedBid) {
    for (BidLifecycleListener delegate : delegates) {
      delegate.onBidCached(cachedBid);
    }
  }
}
