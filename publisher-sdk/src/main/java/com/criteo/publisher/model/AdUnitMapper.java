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

import static com.criteo.publisher.BiddingLogMessage.onInvalidAdUnit;
import static com.criteo.publisher.BiddingLogMessage.onUnsupportedAdFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.util.AdUnitType;
import com.criteo.publisher.util.DeviceUtil;
import java.util.Arrays;
import java.util.Collection;

public class AdUnitMapper {

  /**
   * Special size representing a native ad.
   */
  private static final AdSize NATIVE_SIZE = new AdSize(2, 2);

  /**
   * Only GAM AppBidding is supporting rewarded ads because they are handling the display themselves.
   */
  private static final Collection<Integration> SUPPORTED_INTEGRATION_FOR_REWARDED = Arrays.asList(
      Integration.GAM_APP_BIDDING
  );

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final DeviceUtil deviceUtil;

  @NonNull
  private final IntegrationRegistry integrationRegistry;

  public AdUnitMapper(
      @NonNull DeviceUtil deviceUtil,
      @NonNull IntegrationRegistry integrationRegistry
  ) {
    this.deviceUtil = deviceUtil;
    this.integrationRegistry = integrationRegistry;
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
    if (adUnit == null) {
      return null;
    }

    AdSize size = getSize(adUnit);
    CacheAdUnit cacheAdUnit = new CacheAdUnit(size, adUnit.getAdUnitId(), adUnit.getAdUnitType());

    return filterInvalidCacheAdUnits(cacheAdUnit);
  }

  @NonNull
  private AdSize getSize(@NonNull AdUnit adUnit) {
    switch (adUnit.getAdUnitType()) {
      case CRITEO_BANNER:
        BannerAdUnit bannerAdUnit = (BannerAdUnit) adUnit;
        return bannerAdUnit.getSize();
      case CRITEO_INTERSTITIAL:
      case CRITEO_REWARDED:
        return deviceUtil.getCurrentScreenSize();
      case CRITEO_CUSTOM_NATIVE:
        return NATIVE_SIZE;
      default:
        throw new IllegalArgumentException("Found an invalid AdUnit");
    }
  }

  @Nullable
  private CacheAdUnit filterInvalidCacheAdUnits(@NonNull CacheAdUnit cacheAdUnit) {
    Integration integration = integrationRegistry.readIntegration();

    if (cacheAdUnit.getPlacementId().isEmpty()
        || cacheAdUnit.getSize().getWidth() <= 0
        || cacheAdUnit.getSize().getHeight() <= 0) {
      logger.log(onInvalidAdUnit(cacheAdUnit));
      return null;
    }

    if (cacheAdUnit.getAdUnitType() == AdUnitType.CRITEO_REWARDED && !SUPPORTED_INTEGRATION_FOR_REWARDED.contains(integration)) {
      logger.log(onUnsupportedAdFormat(cacheAdUnit, integration));
      return null;
    }

    return cacheAdUnit;
  }

}
