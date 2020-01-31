package com.criteo.publisher.model;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_INTERSTITIAL;
import static com.criteo.publisher.model.AdUnitMapper.splitIntoChunks;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.res.Configuration;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
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

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenBannerWithNoPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit(null, new AdSize(1, 1));

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
  public void convertValidAdUnits_GivenBannerWithNoSize_SkipIt() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", null);

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
  public void convertValidAdUnits_GivenInterstitialWithNoPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new InterstitialAdUnit(null);

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenInterstitialWithEmptyPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new InterstitialAdUnit("");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).isEmpty();
  }

  @Test
  public void convertValidAdUnits_GivenNativeWithNoPlacementId_SkipIt() throws Exception {
    AdUnit adUnit = new NativeAdUnit(null);

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
  public void convertValidAdUnits_GivenValidBanner_MapIt() throws Exception {
    AdSize size = new AdSize(1, 1);
    AdUnit adUnit = new BannerAdUnit("adUnit", size);

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(singletonList(new CacheAdUnit(size, "adUnit", CRITEO_BANNER)));
  }

  @Test
  public void convertValidAdUnits_GivenValidNative_MapItWithNativeSize() throws Exception {
    // Native have a special size of 2x2 in InventoryDB
    AdSize nativeSize = new AdSize(2, 2);

    AdUnit adUnit = new NativeAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(singletonList(new CacheAdUnit(nativeSize, "adUnit", CRITEO_CUSTOM_NATIVE)));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInPortrait_MapItWithPortraitSize() throws Exception {
    AdSize portraitSize = new AdSize(10, 30);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);
    when(deviceUtil.getSizePortrait()).thenReturn(portraitSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(singletonList(new CacheAdUnit(portraitSize, "adUnit", CRITEO_INTERSTITIAL)));
  }

  @Test
  public void convertValidAdUnits_GivenValidInterstitialAndDeviceInLandscape_MapItWithLandscapeSize() throws Exception {
    AdSize landscapeSize = new AdSize(30, 10);
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);
    when(deviceUtil.getSizeLandscape()).thenReturn(landscapeSize);

    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    List<List<CacheAdUnit>> validAdUnits = mapper.mapToChunks(singletonList(adUnit));

    assertThat(validAdUnits).containsExactly(singletonList(new CacheAdUnit(landscapeSize, "adUnit", CRITEO_INTERSTITIAL)));
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
  public void convertValidAdUnit_GivenListVersionReturningChunkOfNothing_ReturnNull() throws Exception {
    mapper = spy(mapper);
    AdUnit adUnit = mock(AdUnit.class);

    doReturn(singletonList(emptyList())).when(mapper)
        .mapToChunks(singletonList(adUnit));

    CacheAdUnit validAdUnit = mapper.map(adUnit);

    assertThat(validAdUnit).isNull();
  }

  @Test
  public void convertValidAdUnit_GivenListVersionReturningSingleton_ReturnTheSingleElement() throws Exception {
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
  public void splitIntoChunks_GivenChunkSizeAndSampleOfElements_ReturnsSplitChunks() throws Exception {
    assertThat(splitIntoChunks(asList(1), 1))
        .containsExactly(asList(1));

    assertThat(splitIntoChunks(asList(1, 2, 3), 1))
        .containsExactly(asList(1), asList(2), asList(3));
  }

  @Test
  public void splitIntoChunks_GivenChunkSizeAndSampleOfElements_ReturnsSplitChunks2() throws Exception {
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