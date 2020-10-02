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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.InterstitialAdUnit;
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

  @Mock
  private BidResponseListener listener;

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
  public void getBidResponse_GivenNullAdUnit_ReturnNoBid() throws Exception {
    inHouse.loadBidResponse(null, listener);

    verify(listener).onResponse(null);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingNoBid_ReturnNoBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(null);

    inHouse.loadBidResponse(adUnit, listener);

    verify(listener).onResponse(null);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingBid_ReturnBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);

    when(slot.getCpmAsNumber()).thenReturn(42.1337);
    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    inHouse.loadBidResponse(adUnit, listener);

    verify(listener).onResponse(argThat(bidResponse -> {
      assertThat(bidResponse.getPrice()).isEqualTo(42.1337);
      return true;
    }));
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  @Test
  public void getBidResponse_GivenInterstitialAdUnitAndInterstitialNotAvailable_ReturnNoBidWithoutRequestingBidManager() throws Exception {
    InterstitialAdUnit adUnit = new InterstitialAdUnit("myAdUnit");
    when(interstitialActivityHelper.isAvailable()).thenReturn(false);

    inHouse.loadBidResponse(adUnit, listener);

    verify(listener).onResponse(null);
    verifyNoInteractions(bidManager);
    verify(integrationRegistry).declare(Integration.IN_HOUSE);
  }

  private void assertIsNoBid(BidResponse bidResponse) {
    assertThat(bidResponse).isNull();
  }

}