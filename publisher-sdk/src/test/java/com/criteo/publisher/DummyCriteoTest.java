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

package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
    BidListener bidListener = Mockito.mock(BidListener.class);
    criteo.getBidForAdUnit(null, bidListener);
    criteo.getBidForAdUnit(banner, bidListener);
    criteo.getBidForAdUnit(interstitial, bidListener);
    criteo.getBidForAdUnit(aNative, bidListener);
    Mockito.verify(bidListener, times(4)).onNoBid();
  }

  @Test
  public void getBidResponse_GivenAnyAdUnit_ReturnNoBid() throws Exception {
    assertThat(criteo.getBidResponse(banner)).isNull();
    assertThat(criteo.getBidResponse(interstitial)).isNull();
    assertThat(criteo.getBidResponse(aNative)).isNull();
  }

  @Test
  public void getDeviceInfo_ReturnNoUserAgentAndInitializeDirectly() throws Exception {
    DeviceInfo deviceInfo = criteo.getDeviceInfo();
    deviceInfo.initialize();

    assertThat(deviceInfo.getUserAgent().get()).isEmpty();
  }

}