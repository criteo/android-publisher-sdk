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

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.nativeads.NativeAssets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InHouseTest {

  @Mock
  private BidManager bidManager;

  @Mock
  private Clock clock;

  @Mock
  private InterstitialActivityHelper interstitialActivityHelper;

  @Mock
  private IntegrationRegistry integrationRegistry;

  private InHouse inHouse;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    inHouse = new InHouse(
        bidManager,
        clock,
        interstitialActivityHelper,
        integrationRegistry
    );
  }

  @Test
  public void getTokenValue_GivenToken_DelegateToCache() throws Exception {
    BidResponse bidResponse = mock(BidResponse.class);
    when(bidResponse.consumeDisplayUrlFor(CRITEO_BANNER)).thenReturn("display.url");

    String displayUrl = inHouse.getDisplayUrl(bidResponse, CRITEO_BANNER);

    assertThat(displayUrl).isEqualTo(displayUrl);
  }

  @Test
  public void getNativeTokenValue_GivenToken_DelegateToCache() throws Exception {
    NativeAssets expected = mock(NativeAssets.class);
    BidResponse bidResponse = mock(BidResponse.class);
    when(bidResponse.consumeNativeAssets()).thenReturn(expected);

    NativeAssets nativeAssets = inHouse.getNativeAssets(bidResponse);

    assertThat(nativeAssets).isEqualTo(expected);
  }

  @Test
  public void getBidResponse_GivenNullAdUnit_ReturnNoBid() throws Exception {
    BidResponse bidResponse = inHouse.getBidResponse(null);

    assertIsNoBid(bidResponse);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingNoBid_ReturnNoBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(null);

    BidResponse bidResponse = inHouse.getBidResponse(adUnit);

    assertIsNoBid(bidResponse);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingBid_ReturnBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);

    when(slot.getCpmAsNumber()).thenReturn(42.1337);
    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    BidResponse bidResponse = inHouse.getBidResponse(adUnit);

    assertThat(bidResponse.getPrice()).isEqualTo(42.1337);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void getBidResponse_GivenInterstitialAdUnitAndInterstitialNotAvailable_ReturnNoBidWithoutRequestingBidManager() throws Exception {
    InterstitialAdUnit adUnit = new InterstitialAdUnit("myAdUnit");
    when(interstitialActivityHelper.isAvailable()).thenReturn(false);

    BidResponse bidResponse = inHouse.getBidResponse(adUnit);

    assertIsNoBid(bidResponse);
    verifyZeroInteractions(bidManager);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  private void assertIsNoBid(BidResponse bidResponse) {
    assertThat(bidResponse).isEqualTo(BidResponse.NO_BID);
  }

}