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
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.util.AdUnitType;
import java.util.Map;

public class OtherAdServersHeaderBidding implements HeaderBiddingHandler {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  private final Logger logger = LoggerFactory.getLogger(getClass());

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
  public void enrichBid(
      @NonNull Object object,
      @NonNull AdUnitType adUnitType,
      @NonNull CdbResponseSlot slot
  ) {
    if (!canHandle(object)) {
      return;
    }

    Map map = (Map) object;
    map.put(CRT_DISPLAY_URL, slot.getDisplayUrl());
    map.put(CRT_CPM, slot.getCpm());

    String description = CRT_DISPLAY_URL + "=" + slot.getDisplayUrl() + "," + CRT_CPM + "=" + slot.getCpm();

    if (adUnitType == AdUnitType.CRITEO_BANNER) {
      String size = slot.getWidth() + "x" + slot.getHeight();
      map.put(CRT_SIZE, size);
      description = description + "," + CRT_SIZE + "=" + size;
    }

    logger.log(AppBiddingLogMessage.onAdObjectEnrichedSuccessfully(getIntegration(), description));
  }

}
