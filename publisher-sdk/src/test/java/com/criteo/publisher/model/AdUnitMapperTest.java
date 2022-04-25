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

import static com.criteo.publisher.model.AdUnitMapper.splitIntoChunks;
import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL;
import static com.criteo.publisher.util.AdUnitType.CRITEO_REWARDED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.util.DeviceUtil;
import java.util.List;
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

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("", new AdSize(1, 1));

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithZeroSize_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(0, 0));

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenInterstitialWithEmptyPlacementId_SkipIt() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(1, 2));

    AdUnit adUnit = new InterstitialAdUnit("");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenNativeWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new NativeAdUnit("");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenRewardedWithEmptyPlacementId_SkipIt() throws Exception {
    when(deviceUtil.getCurrentScreenSize()).thenReturn(new AdSize(1, 2));

    AdUnit adUnit = new RewardedAdUnit("");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenValidBanner_MapIt() throws Exception {
    AdSize size = new AdSize(1, 1);
    AdUnit adUnit = new BannerAdUnit("adUnit", size);

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits)
        .containsExactly(singletonList(new CacheAdUnit(size, "adUnit", CRITEO_BANNER)));
  }

  @Test
  public void convertValidAdUnits_GivenValidNative_MapItWithNativeSize() throws Exception {
    // Native have a special size of 2x2 in InventoryDB
    AdSize nativeSize = new AdSize(2, 2);

    AdUnit adUnit = new NativeAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(
        singletonList(new CacheAdUnit(nativeSize, "adUnit", CRITEO_CUSTOM_NATIVE)));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInPortrait_MapItWithPortraitSize()
      throws Exception {
    AdSize portraitSize = new AdSize(10, 30);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(portraitSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(
        singletonList(new CacheAdUnit(portraitSize, "adUnit", CRITEO_INTERSTITIAL)));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInLandscape_MapItWithLandscapeSize()
      throws Exception {
    AdSize landscapeSize = new AdSize(30, 10);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(landscapeSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(
        singletonList(new CacheAdUnit(landscapeSize, "adUnit", CRITEO_INTERSTITIAL)));
  }

  @Test
  public void convertValidAdUnits_GivenValidRewarded_AndUnsupportedIntegration_FilterIt() throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.IN_HOUSE);

    AdSize portraitSize = new AdSize(10, 30);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(portraitSize);

    AdUnit adUnit = new RewardedAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenValidRewardedAndDeviceInPortrait_AndSupportedIntegration_MapItWithPortraitSize()
      throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.GAM_APP_BIDDING);

    AdSize portraitSize = new AdSize(10, 30);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(portraitSize);

    AdUnit adUnit = new RewardedAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(
        singletonList(new CacheAdUnit(portraitSize, "adUnit", CRITEO_REWARDED)));
  }

  @Test
  public void convertValidAdUnits_GivenValidRewardedAndDeviceInLandscape_AndSupportedIntegration_MapItWithLandscapeSize()
      throws Exception {
    when(integrationRegistry.readIntegration()).thenReturn(Integration.GAM_APP_BIDDING);

    AdSize landscapeSize = new AdSize(30, 10);
    when(deviceUtil.getCurrentScreenSize()).thenReturn(landscapeSize);

    AdUnit adUnit = new RewardedAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(
        singletonList(new CacheAdUnit(landscapeSize, "adUnit", CRITEO_REWARDED)));
  }

  @Test
  public void convertValidAdUnits_GivenSameAdUnits_MergeThem() throws Exception {
    AdSize size = new AdSize(1, 1);
    AdUnit adUnit1 = new BannerAdUnit("adUnit1", size);
    AdUnit adUnit2 = new BannerAdUnit("adUnit2", size);

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(asList(
        adUnit1,
        adUnit2,
        adUnit2,
        adUnit1));

    assertThat(validAdUnits).hasSize(1);
    assertThat(validAdUnits.get(0)).containsExactlyInAnyOrder(
        new CacheAdUnit(size, "adUnit1", CRITEO_BANNER),
        new CacheAdUnit(size, "adUnit2", CRITEO_BANNER)
    );
  }

  @Test
  public void convertValidAdUnit_GivenListVersionReturningNothing_ReturnNull() throws Exception {
    mapper = spy(mapper);
    AdUnit adUnit = mock(AdUnit.class);

    doReturn(emptyList()).when(mapper)
        .mapToChunks(singletonList(adUnit));

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnit_GivenListVersionReturningChunkOfNothing_ReturnNull()
      throws Exception {
    mapper = spy(mapper);
    AdUnit adUnit = mock(AdUnit.class);

    doReturn(singletonList(emptyList())).when(mapper)
        .mapToChunks(singletonList(adUnit));

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnit_GivenListVersionReturningSingleton_ReturnTheSingleElement()
      throws Exception {
    mapper = spy(mapper);
    AdUnit adUnit = mock(AdUnit.class);
    CacheAdUnit expectedAdUnit = new CacheAdUnit(new AdSize(1, 1), "adUnit", CRITEO_BANNER);

    doReturn(singletonList(singletonList(expectedAdUnit))).when(mapper)
        .mapToChunks(singletonList(adUnit));

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isSameAs(expectedAdUnit);
  }

  @Test
  public void convertValidAdUnit_GivenNull_ReturnNull() throws Exception {
    CacheAdUnit validAdUnit = mapper.map(null);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void splitIntoChunks_GivenAnyChunkSizeAndNoElements_ReturnsEmpty() throws Exception {
    List<List<Object>> chunks = splitIntoChunks(emptyList(), 1);

    assertThat(chunks).isEmpty();
  }

  @Test
  public void splitIntoChunks_GivenAnyChunkSizeAndNoElements_ReturnsEmpty2() throws Exception {
    List<List<Object>> chunks = splitIntoChunks(emptyList(), 1000);

    assertThat(chunks).isEmpty();
  }

  @Test
  public void splitIntoChunks_GivenChunkSizeAndSampleOfElements_ReturnsSplitChunks()
      throws Exception {
    assertThat(splitIntoChunks(asList(1), 1))
        .containsExactly(asList(1));

    assertThat(splitIntoChunks(asList(1, 2, 3), 1))
        .containsExactly(asList(1), asList(2), asList(3));
  }

  @Test
  public void splitIntoChunks_GivenChunkSizeAndSampleOfElements_ReturnsSplitChunks2()
      throws Exception {
    assertThat(splitIntoChunks(asList(1), 2))
        .containsExactly(asList(1));

    assertThat(splitIntoChunks(asList(1, 2), 2))
        .containsExactly(asList(1, 2));

    assertThat(splitIntoChunks(asList(1, 2, 3), 2))
        .containsExactly(asList(1, 2), asList(3));

    assertThat(splitIntoChunks(asList(1, 2, 3, 4), 2))
        .containsExactly(asList(1, 2), asList(3, 4));
  }

}
