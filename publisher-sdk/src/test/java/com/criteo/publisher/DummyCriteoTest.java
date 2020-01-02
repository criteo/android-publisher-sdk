package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class DummyCriteoTest {

  private final BannerAdUnit banner = new BannerAdUnit("banner", new AdSize(1337, 42));
  private final InterstitialAdUnit interstitial = new InterstitialAdUnit("interstitial");
  private final NativeAdUnit aNative = new NativeAdUnit("native");

  private DummyCriteo criteo;

  @Before
  public void setUp() throws Exception {
    criteo = new DummyCriteo();
  }

  @Test
  public void setBidsForAdUnit_GivenAnyAdUnit_DoNothingAndDoNotThrow() throws Exception {
    assertThatCode(() -> {
      criteo.setBidsForAdUnit(null, null);
    }).doesNotThrowAnyException();

    assertThatCode(() -> {
      criteo.setBidsForAdUnit(new HashMap<>(), banner);
    }).doesNotThrowAnyException();

    assertThatCode(() -> {
      criteo.setBidsForAdUnit(new HashMap<>(), interstitial);
    }).doesNotThrowAnyException();

    assertThatCode(() -> {
      criteo.setBidsForAdUnit(new HashMap<>(), aNative);
    }).doesNotThrowAnyException();
  }

  @Test
  public void getBidForAdUnit_GivenAnyAdUnit_ReturnNull() throws Exception {
    assertThat(criteo.getBidForAdUnit(null)).isNull();
    assertThat(criteo.getBidForAdUnit(banner)).isNull();
    assertThat(criteo.getBidForAdUnit(interstitial)).isNull();
    assertThat(criteo.getBidForAdUnit(aNative)).isNull();
  }

  @Test
  public void getBidResponse_GivenAnyAdUnit_ReturnNoBid() throws Exception {
    BidResponse noBidResponse = new BidResponse(0.0, null, false);

    assertThat(criteo.getBidResponse(null)).isEqualTo(noBidResponse);
    assertThat(criteo.getBidResponse(banner)).isEqualTo(noBidResponse);
    assertThat(criteo.getBidResponse(interstitial)).isEqualTo(noBidResponse);
    assertThat(criteo.getBidResponse(aNative)).isEqualTo(noBidResponse);
  }

  @Test
  public void getTokenValue_GivenAnyAdUnit_ReturnNull() throws Exception {
    BidToken bannerToken = new BidToken(UUID.randomUUID(), banner);
    BidToken interstitialToken = new BidToken(UUID.randomUUID(), interstitial);
    BidToken nativeToken = new BidToken(UUID.randomUUID(), aNative);

    assertThat(criteo.getTokenValue(null, null)).isNull();
    assertThat(criteo.getTokenValue(bannerToken, AdUnitType.CRITEO_BANNER)).isNull();
    assertThat(criteo.getTokenValue(interstitialToken, AdUnitType.CRITEO_INTERSTITIAL)).isNull();
    assertThat(criteo.getTokenValue(nativeToken, AdUnitType.CRITEO_CUSTOM_NATIVE)).isNull();
    assertThat(criteo.getTokenValue(bannerToken, AdUnitType.CRITEO_INTERSTITIAL)).isNull();
  }

  @Test
  public void getDeviceInfo_ReturnNoUserAgentAndInitializeDirectly() throws Exception {
    UserAgentCallback callback = mock(UserAgentCallback.class);

    DeviceInfo deviceInfo = criteo.getDeviceInfo();
    deviceInfo.initialize(callback);

    verify(callback).done();
    assertThat(deviceInfo.getUserAgent().get()).isEmpty();
  }

}