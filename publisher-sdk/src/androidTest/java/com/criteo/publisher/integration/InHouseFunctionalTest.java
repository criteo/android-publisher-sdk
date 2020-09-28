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

package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import androidx.annotation.NonNull;
import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;

public class InHouseFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;

  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;
  private final InterstitialAdUnit invalidInterstitialAdUnit = TestAdUnits.INTERSTITIAL_UNKNOWN;

  @Inject
  private Context context;

  @SpyBean
  private PubSdkApi api;

  @Test
  public void loadBannerAd_GivenValidAdUnit_ThenListenerIsNotifiedOfTheSuccess() throws Exception {
    Criteo criteo = givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(validBannerAdUnit);
    bannerView.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    verify(listener).onAdReceived(bannerView);

    verify(api, atLeastOnce()).loadCdb(
        argThat(request -> request.getProfileId() == Integration.IN_HOUSE.getProfileId()),
        any()
    );
  }

  @Test
  public void loadBannerAd_GivenInvalidAdUnit_ThenListenerIsNotifiedOfTheFailure()
      throws Exception {
    Criteo criteo = givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(invalidBannerAdUnit);
    bannerView.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  @Test
  public void loadBannerAd_GivenValidBannerAndTokenUsedTwice_ThenListenerIsNotifiedOfSuccessFirstThenFailure()
      throws Exception {
    Criteo criteo = givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(validBannerAdUnit);
    bannerView.loadAd(bidResponse.getBidToken());
    bannerView.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onAdReceived(bannerView);
    inOrder.verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void loadInterstitialAd_GivenValidAdUnit_ThenListenerIsNotifiedOfTheSuccess() throws Exception {
    Criteo criteo = givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(validInterstitialAdUnit);
    interstitial.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    verify(listener).onAdReceived(interstitial);

    verify(api, atLeastOnce()).loadCdb(
        argThat(request -> request.getProfileId() == Integration.IN_HOUSE.getProfileId()),
        any()
    );
  }

  @Test
  public void loadInterstitialAd_GivenInvalidAdUnit_ThenListenerIsNotifiedOfTheFailure()
      throws Exception {
    Criteo criteo = givenInitializedSdk(invalidInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(invalidInterstitialAdUnit);
    interstitial.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  @Test
  public void loadInterstitialAd_GivenValidInterstitialAndTokenUsedTwice_ThenListenerIsNotifiedOfSuccessFirstThenFailure()
      throws Exception {
    Criteo criteo = givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(validInterstitialAdUnit);
    interstitial.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    interstitial.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onAdReceived(interstitial);
    inOrder.verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    inOrder.verifyNoMoreInteractions();
  }

  @NonNull
  private CriteoBannerView createBannerView() {
    return callOnMainThreadAndWait(() -> new CriteoBannerView(context));
  }

  @NonNull
  private CriteoInterstitial createInterstitial() {
    return callOnMainThreadAndWait(CriteoInterstitial::new);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

  private Criteo givenInitializedSdk(AdUnit... adUnits) throws CriteoInitException {
    Criteo criteo = givenInitializedCriteo(adUnits);
    waitForIdleState();
    return criteo;
  }

}
