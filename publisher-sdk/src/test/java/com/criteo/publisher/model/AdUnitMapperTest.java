package com.criteo.publisher.model;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_INTERSTITIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.res.Configuration;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdUnitMapperTest {

  @Mock
  private AndroidUtil androidUtil;

  @Mock
  private DeviceUtil deviceUtil;

  private AdUnitMapper mapper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    mapper = new AdUnitMapper(androidUtil, deviceUtil);
  }

  @Test
  public void convertValidAdUnits_GivenNullElement_SkipIt() throws Exception {
    AdUnit adUnit = null;

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithNoPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit(null, new AdSize(1, 1));

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("", new AdSize(1, 1));

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithNoSize_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", null);

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithZeroSize_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(0, 0));

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenInterstitialWithNoPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new InterstitialAdUnit(null);

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenInterstitialWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new InterstitialAdUnit("");

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenNativeWithNoPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new NativeAdUnit(null);

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenNativeWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new NativeAdUnit("");

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenValidBanner_MapIt() throws Exception {
    AdSize size = new AdSize(1, 1);
    AdUnit adUnit = new BannerAdUnit("adUnit", size);

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(new CacheAdUnit(size, "adUnit", CRITEO_BANNER));
  }

  @Test
  public void convertValidAdUnits_GivenValidNative_MapItWithNativeSize() throws Exception {
    // Native have a special size of 2x2 in InventoryDB
    AdSize nativeSize = new AdSize(2, 2);

    AdUnit adUnit = new NativeAdUnit("adUnit");

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(new CacheAdUnit(nativeSize, "adUnit", CRITEO_CUSTOM_NATIVE));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInPortrait_MapItWithPortraitSize() throws Exception {
    AdSize portraitSize = new AdSize(10, 30);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);
    when(deviceUtil.getSizePortrait()).thenReturn(portraitSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(new CacheAdUnit(portraitSize, "adUnit", CRITEO_INTERSTITIAL));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInLandscape_MapItWithLandscapeSize() throws Exception {
    AdSize landscapeSize = new AdSize(30, 10);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);
    when(deviceUtil.getSizeLandscape()).thenReturn(landscapeSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    List<CacheAdUnit> validAdUnits = mapper.convertValidAdUnits(Collections.singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(new CacheAdUnit(landscapeSize, "adUnit", CRITEO_INTERSTITIAL));
  }

  @Test
  public void convertValidAdUnit_GivenListVersionReturningNothing_ReturnNull() throws Exception {
    mapper = spy(mapper);
    AdUnit adUnit = mock(AdUnit.class);

    doReturn(Collections.emptyList()).when(mapper)
        .convertValidAdUnits(Collections.singletonList(adUnit));

    CacheAdUnit validAdUnit = mapper.convertValidAdUnit(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnit_GivenListVersionReturningSingleton_ReturnTheSingleElement() throws Exception {
    mapper = spy(mapper);
    AdUnit adUnit = mock(AdUnit.class);
    CacheAdUnit expectedAdUnit = new CacheAdUnit(new AdSize(1, 1), "adUnit", CRITEO_BANNER);

    doReturn(Collections.singletonList(expectedAdUnit)).when(mapper)
        .convertValidAdUnits(Collections.singletonList(adUnit));

    CacheAdUnit validAdUnit = mapper.convertValidAdUnit(adUnit);

    assertThat(validAdUnit).isSameAs(expectedAdUnit);
  }

  @Test
  public void convertValidAdUnit_GivenNull_ReturnNull() throws Exception {
    CacheAdUnit validAdUnit = mapper.convertValidAdUnit(null);

    assertThat(validAdUnit).isNull();
  }

}