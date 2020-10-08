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

package com.criteo.publisher.cache;

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.DeviceUtil;
import java.util.HashMap;
import java.util.Map;

public class SdkCache {

  private final Map<CacheAdUnit, CdbResponseSlot> slotMap;
  private final DeviceUtil deviceUtil;

  public SdkCache(@NonNull DeviceUtil deviceUtil) {
    slotMap = new HashMap<>();
    this.deviceUtil = deviceUtil;
  }

  public void add(@NonNull CdbResponseSlot slot) {
    CacheAdUnit key = detectCacheAdUnit(slot);
    if (key != null) {
      slotMap.put(key, slot);
    }
  }

  @Nullable
  public CacheAdUnit detectCacheAdUnit(@NonNull CdbResponseSlot slot) {
    String placementId = slot.getPlacementId();
    if (placementId == null) {
      return null;
    }

    AdUnitType adUnitType = findAdUnitType(slot);
    return new CacheAdUnit(new AdSize(slot.getWidth(), slot.getHeight()), placementId, adUnitType);
  }

  // FIXME: EE-608
  private AdUnitType findAdUnitType(CdbResponseSlot slot) {
    if (slot.isNative()) {
      return CRITEO_CUSTOM_NATIVE;
    }

    AdSize currentScreenSize = deviceUtil.getCurrentScreenSize();
    AdSize transposedScreenSize = transpose(currentScreenSize);
    AdSize slotSize = new AdSize(slot.getWidth(), slot.getHeight());

    if (slotSize.equals(currentScreenSize) || slotSize.equals(transposedScreenSize)) {
      return CRITEO_INTERSTITIAL;
    }

    return CRITEO_BANNER;
  }

  @NonNull
  private AdSize transpose(@NonNull AdSize size) {
    return new AdSize(size.getHeight(), size.getWidth());
  }

  /**
   * Get the slot corresponding to the given key.
   * <p>
   * If no slot match the given key, then <code>null</code> is returned.
   *
   * @param key of the slot to look for
   * @return found slot or null if not found
   */
  @Nullable
  public CdbResponseSlot peekAdUnit(CacheAdUnit key) {
    return slotMap.get(key);
  }

  public void remove(CacheAdUnit key) {
    slotMap.remove(key);
  }

  @VisibleForTesting
  int getItemCount() {
    return slotMap.size();
  }

  @VisibleForTesting
  public void put(@NonNull CacheAdUnit cacheAdUnit, @Nullable CdbResponseSlot slot) {
    slotMap.put(cacheAdUnit, slot);
  }
}
