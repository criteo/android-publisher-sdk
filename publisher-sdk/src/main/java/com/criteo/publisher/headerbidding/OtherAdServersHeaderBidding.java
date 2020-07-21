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

package com.criteo.publisher.headerbidding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Slot;
import java.util.Map;

public class OtherAdServersHeaderBidding implements HeaderBiddingHandler {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  @Override
  public boolean canHandle(@NonNull Object object) {
    return object instanceof Map;
  }

  @NonNull
  @Override
  public Integration getIntegration() {
    return Integration.CUSTOM_APP_BIDDING;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void cleanPreviousBid(@NonNull Object object) {
    if (!canHandle(object)) {
      return;
    }

    Map map = (Map) object;
    map.remove(CRT_CPM);
    map.remove(CRT_DISPLAY_URL);
    map.remove(CRT_SIZE);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit, @NonNull Slot slot) {
    if (!canHandle(object)) {
      return;
    }

    Map map = (Map) object;
    map.put(CRT_DISPLAY_URL, slot.getDisplayUrl());
    map.put(CRT_CPM, slot.getCpm());

    if (adUnit instanceof BannerAdUnit) {
      map.put(CRT_SIZE, slot.getWidth() + "x" + slot.getHeight());
    }
  }

}
