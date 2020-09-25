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

package com.criteo.publisher.model;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.DeviceUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdUnitMapper {

  private static final String TAG = AdUnitMapper.class.getSimpleName();

  /**
   * Ad units are grouped into chunks so bid request size stay reasonable and this may improve the
   * situation in case of flaky network.
   * <p>
   * This constant is set given a CDB suggestion:
   * <ul>
   *   <li>RTB does not handle too many slots</li>
   *   <li>Arbitrage is optimized to process 8 slots in parallel</li>
   * </ul>
   * <p>
   * Although, the reason may change over time and it would require a proper study.
   */
  private static final int CHUNK_SIZE = 8;

  /**
   * Special size representing a native ad.
   */
  private static final AdSize NATIVE_SIZE = new AdSize(2, 2);

  private final AndroidUtil androidUtil;
  private final DeviceUtil deviceUtil;

  public AdUnitMapper(AndroidUtil androidUtil, DeviceUtil deviceUtil) {
    this.androidUtil = androidUtil;
    this.deviceUtil = deviceUtil;
  }

  /**
   * Transform the given valid {@link AdUnit} into internal {@link CacheAdUnit}.
   * <p>
   * Valid ad units are transformed and collected while invalid ad units are ignored. See {@link
   * #map(AdUnit)} for validity rules.
   * <p>
   * Collected ad units are then grouped into chunks to load.
   *
   * @param adUnits to transform
   * @return chunks of internal ad unit representations
   */
  public List<List<CacheAdUnit>> mapToChunks(@NonNull List<AdUnit> adUnits) {
    Set<CacheAdUnit> cacheAdUnits = new HashSet<>();
    for (AdUnit adUnit : adUnits) {
      if (adUnit == null) {
        continue;
      }

      AdSize size = getSize(adUnit);
      CacheAdUnit cacheAdUnit = new CacheAdUnit(size, adUnit.getAdUnitId(), adUnit.getAdUnitType());
      cacheAdUnits.add(cacheAdUnit);
    }
    return splitIntoChunks(filterInvalidCacheAdUnits(cacheAdUnits), CHUNK_SIZE);
  }

  @NonNull
  private AdSize getSize(@NonNull AdUnit adUnit) {
    switch (adUnit.getAdUnitType()) {
      case CRITEO_BANNER:
        BannerAdUnit bannerAdUnit = (BannerAdUnit) adUnit;
        return bannerAdUnit.getSize();
      case CRITEO_INTERSTITIAL:
        return deviceUtil.getCurrentScreenSize();
      case CRITEO_CUSTOM_NATIVE:
        return NATIVE_SIZE;
      default:
        throw new IllegalArgumentException("Found an invalid AdUnit");
    }
  }

  /**
   * Transform the given {@link AdUnit} into an internal {@link CacheAdUnit} if valid
   * <p>
   * The given ad unit is considered valid if all those conditions are met:
   * <ul>
   *   <li>not null</li>
   *   <li>placement ID is not empty nor null</li>
   *   <li>width is strictly positive</li>
   *   <li>height is strictly positive</li>
   * </ul>
   * <p>
   * If the ad unit is not valid, then <code>null</code> is returned instead.
   *
   * @param adUnit to transform
   * @return internal ad unit representation or <code>null</code> if given ad unit is invalid
   */
  @Nullable
  public CacheAdUnit map(@Nullable AdUnit adUnit) {
    List<List<CacheAdUnit>> validAdUnits = mapToChunks(Collections.singletonList(adUnit));
    if (validAdUnits.isEmpty() || validAdUnits.get(0).isEmpty()) {
      return null;
    } else {
      return validAdUnits.get(0).get(0);
    }
  }

  private List<CacheAdUnit> filterInvalidCacheAdUnits(Collection<CacheAdUnit> cacheAdUnits) {
    List<CacheAdUnit> validatedCacheAdUnits = new ArrayList<>();

    for (CacheAdUnit cacheAdUnit : cacheAdUnits) {
      if (cacheAdUnit.getPlacementId().isEmpty()
          || cacheAdUnit.getSize().getWidth() <= 0
          || cacheAdUnit.getSize().getHeight() <= 0) {
        Log.e(TAG, "Found an invalid AdUnit: " + cacheAdUnit);
        continue;
      }
      validatedCacheAdUnits.add(cacheAdUnit);
    }

    return validatedCacheAdUnits;
  }

  /**
   * Returns consecutive {@linkplain List#subList(int, int) sub-lists} of given list, each of the
   * same size (the last list may be smaller).
   * <p>
   * For example, splitting a list containing <code>[a, b, c, d, e]</code> with a chunk size of 3
   * yields <code>[[a, b, c], [d, e]]]</code>.
   *
   * @param elements  the list to return consecutive sub-lists of
   * @param chunkSize the desired size of each sub-lists (the last may be smaller)
   * @return a list of consecutive sub-lists
   */
  @VisibleForTesting
  static <T> List<List<T>> splitIntoChunks(List<T> elements, int chunkSize) {
    if (elements.isEmpty()) {
      return Collections.emptyList();
    }

    List<List<T>> chunks = new ArrayList<>();

    for (int from = 0; from < elements.size(); from += chunkSize) {
      int to = Math.min(from + chunkSize, elements.size());
      chunks.add(elements.subList(from, to));
    }

    return chunks;
  }

}
