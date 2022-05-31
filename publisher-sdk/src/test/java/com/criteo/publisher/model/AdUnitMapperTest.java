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

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL;
import static com.criteo.publisher.util.AdUnitType.CRITEO_REWARDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.util.DeviceUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AdUnitMapperTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private DeviceUtil deviceUtil;

  @Mock
  private IntegrationRegistry integrationRegistry;

  private AdUnitMapper mapper;

  @Before
  public void setUp() throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.FALLBACK);

    mapper = new AdUnitMapper(deviceUtil, integrationRegistry);
  }

  @Test
  public void convertValidAdUnits_GivenNullElement_SkipIt() throws Exception {
    AdUnit adUnit = null;

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("", new AdSize(1, 1));

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithZeroSize_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(0, 0));

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenInterstitialWithEmptyPlacementId_SkipIt() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(1, 2));

    AdUnit adUnit = new InterstitialAdUnit("");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenNativeWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new NativeAdUnit("");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenRewardedWithEmptyPlacementId_SkipIt() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(1, 2));

    AdUnit adUnit = new RewardedAdUnit("");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenValidBanner_MapIt() throws Exception {
    AdSize size = new AdSize(1, 1);
    AdUnit adUnit = new BannerAdUnit("adUnit", size);

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isEqualTo(new CacheAdUnit(size, "adUnit", CRITEO_BANNER));
  }

  @Test
  public void convertValidAdUnits_GivenValidNative_MapItWithNativeSize() throws Exception {
    // Native have a special size of 2x2 in InventoryDB
    AdSize nativeSize = new AdSize(2, 2);

    AdUnit adUnit = new NativeAdUnit("adUnit");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isEqualTo(new CacheAdUnit(nativeSize, "adUnit", CRITEO_CUSTOM_NATIVE));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInPortrait_MapItWithPortraitSize()
      throws Exception {
    AdSize portraitSize = new AdSize(10, 30);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(portraitSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isEqualTo(new CacheAdUnit(portraitSize, "adUnit", CRITEO_INTERSTITIAL));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInLandscape_MapItWithLandscapeSize()
      throws Exception {
    AdSize landscapeSize = new AdSize(30, 10);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(landscapeSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isEqualTo(new CacheAdUnit(landscapeSize, "adUnit", CRITEO_INTERSTITIAL));
  }

  @Test
  public void convertValidAdUnits_GivenValidRewarded_AndUnsupportedIntegration_FilterIt() throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.IN_HOUSE);

    AdSize portraitSize = new AdSize(10, 30);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(portraitSize);

    AdUnit adUnit = new RewardedAdUnit("adUnit");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnits_GivenValidRewardedAndDeviceInPortrait_AndSupportedIntegration_MapItWithPortraitSize()
      throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.GAM_APP_BIDDING);

    AdSize portraitSize = new AdSize(10, 30);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(portraitSize);

    AdUnit adUnit = new RewardedAdUnit("adUnit");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isEqualTo(new CacheAdUnit(portraitSize, "adUnit", CRITEO_REWARDED));
  }

  @Test
  public void convertValidAdUnits_GivenValidRewardedAndDeviceInLandscape_AndSupportedIntegration_MapItWithLandscapeSize()
      throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.GAM_APP_BIDDING);

    AdSize landscapeSize = new AdSize(30, 10);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(landscapeSize);

    AdUnit adUnit = new RewardedAdUnit("adUnit");

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isEqualTo(new CacheAdUnit(landscapeSize, "adUnit", CRITEO_REWARDED));
  }

}
