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
import static com.criteo.publisher.TestAdUnits.INTERSTITIAL_UNKNOWN;
import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import androidx.annotation.NonNull;
import com.criteo.publisher.Bid;
import com.criteo.publisher.BiddingLogMessage;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.RewardedAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.DeviceUtil;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InOrder;

@RunWith(Parameterized.class)
public class InHouseFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule()
      .withSpiedLogger();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;

  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;
  private final InterstitialAdUnit invalidInterstitialAdUnit = INTERSTITIAL_UNKNOWN;

  private final RewardedAdUnit validRewardedAdUnit = TestAdUnits.REWARDED;

  @Parameters(name = "{0}")
  public static Iterable<?> data() {
    return Arrays.asList(false, true);
  }

  @Parameter
  public boolean isLiveBiddingEnabled;

  @Inject
  private Context context;

  @SpyBean
  private PubSdkApi api;

  @SpyBean
  private InterstitialActivityHelper interstitialActivityHelper;

  @SpyBean
  private Config config;

  @MockBean
  private Logger logger;

  @Inject
  private IntegrationRegistry integrationRegistry;

  @Inject
  private DeviceUtil deviceUtil;

  @Before
  public void setUp() throws Exception {
    doReturn(isLiveBiddingEnabled).when(config).isLiveBiddingEnabled();
  }

  @Test
  public void loadRewardedAd_GivenValidAdUnitAndAppBiddingPreviouslyUsed_ThenListenerIsNotifiedOfTheFailure() throws Exception {
    integrationRegistry.declare(Integration.GAM_APP_BIDDING);

    givenInitializedSdk(validRewardedAdUnit);
    clearInvocations(api);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    Bid bid = loadBid(validRewardedAdUnit);
    bannerView.loadAd(bid);
    waitForIdleState();

    assertThat(bid).isNotNull(); // the bid was originally done for app bidding, although there should be no display
    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);

    assertBidRequestHasGoodProfileId();
  }

  @Test
  public void loadRewardedAd_GivenValidAdUnitAndInHouseNotUsed_ThenListenerIsNotifiedOfTheFailure() throws Exception {
    givenInitializedSdk(validRewardedAdUnit);
    clearInvocations(api, logger);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    Bid bid = loadBid(validRewardedAdUnit);
    bannerView.loadAd(bid);
    waitForIdleState();

    assertThat(bid).isNull();
    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(logger).log(BiddingLogMessage.onUnsupportedAdFormat(toCacheAdUnit(validRewardedAdUnit), Integration.FALLBACK));
    verify(api, never()).loadCdb(any(), any());

    assertBidRequestHasGoodProfileId();
  }

  @Test
  public void loadRewardedAd_GivenValidAdUnitAndInHouseAlreadyUsed_ThenListenerIsNotifiedOfTheFailure() throws Exception {
    integrationRegistry.declare(Integration.IN_HOUSE);

    givenInitializedSdk(validRewardedAdUnit);
    clearInvocations(api, logger);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);
    waitForIdleState();
    clearInvocations(listener);

    Bid bid = loadBid(validRewardedAdUnit);
    bannerView.loadAd(bid);
    waitForIdleState();

    assertThat(bid).isNull();
    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(logger).log(BiddingLogMessage.onUnsupportedAdFormat(toCacheAdUnit(validRewardedAdUnit), Integration.IN_HOUSE));
    verify(api, never()).loadCdb(any(), any());

    assertBidRequestHasGoodProfileId();
  }

  private CacheAdUnit toCacheAdUnit(RewardedAdUnit adUnit) {
    return new CacheAdUnit(
        deviceUtil.getCurrentScreenSize(),
        adUnit.getAdUnitId(),
        adUnit.getAdUnitType()
    );
  }

  @Test
  public void loadBannerAd_GivenValidAdUnit_ThenListenerIsNotifiedOfTheSuccess() throws Exception {
    givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    Bid bid = loadBid(validBannerAdUnit);
    bannerView.loadAd(bid);
    waitForIdleState();

    verify(listener).onAdReceived(bannerView);

    assertBidRequestHasGoodProfileId();
  }

  @Test
  public void loadBannerAd_GivenInvalidAdUnit_ThenListenerIsNotifiedOfTheFailure()
      throws Exception {
    givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    Bid bid = loadBid(invalidBannerAdUnit);
    bannerView.loadAd(bid);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  @Test
  public void loadBannerAd_GivenValidBannerAndTokenUsedTwice_ThenListenerIsNotifiedOfSuccessFirstThenFailure()
      throws Exception {
    givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView();
    bannerView.setCriteoBannerAdListener(listener);

    Bid bid = loadBid(validBannerAdUnit);
    bannerView.loadAd(bid);
    bannerView.loadAd(bid);
    waitForIdleState();

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onAdReceived(bannerView);
    inOrder.verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void loadInterstitialAd_GivenValidAdUnit_ThenListenerIsNotifiedOfTheSuccess() throws Exception {
    givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    Bid bid = loadBid(validInterstitialAdUnit);
    interstitial.loadAd(bid);
    waitForIdleState();

    verify(listener).onAdReceived(interstitial);

    assertBidRequestHasGoodProfileId();
  }

  @Test
  public void loadInterstitialAd_GivenInvalidAdUnit_ThenListenerIsNotifiedOfTheFailure()
      throws Exception {
    givenInitializedSdk(invalidInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    Bid bid = loadBid(invalidInterstitialAdUnit);
    interstitial.loadAd(bid);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  @Test
  public void loadInterstitialAd_GivenValidAdUnitButInterstitialActivityNotAvailable_ThenListenerIsNotifiedOfTheFailure()
      throws Exception {
    when(interstitialActivityHelper.isAvailable()).thenReturn(false);

    givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    Bid bid = loadBid(validInterstitialAdUnit);
    interstitial.loadAd(bid);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  @Test
  public void loadInterstitialAd_GivenValidInterstitialAndTokenUsedTwice_ThenListenerIsNotifiedOfSuccessFirstThenFailure()
      throws Exception {
    givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    Bid bid = loadBid(validInterstitialAdUnit);

    interstitial.loadAd(bid);
    waitForIdleState();

    interstitial.loadAd(bid);
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

  private void givenInitializedSdk(AdUnit... adUnits) throws CriteoInitException {
    if (config.isLiveBiddingEnabled()) {
      // Empty it to show that prefetch has no influence
      adUnits = new AdUnit[]{};
    }

    givenInitializedCriteo(adUnits);

    waitForIdleState();
  }

  private void assertBidRequestHasGoodProfileId() throws Exception {
    // InHouse declaration is delayed when bid is consumed. So the declaration is only visible for
    // the next bid.
    loadBid(INTERSTITIAL_UNKNOWN);

    verify(api, atLeastOnce()).loadCdb(
        argThat(request -> request.getProfileId() == Integration.IN_HOUSE.getProfileId()),
        any()
    );
  }

  private Bid loadBid(AdUnit adUnit) {
    AtomicReference<Bid> bidResponseRef = new AtomicReference<>();
    Criteo.getInstance().loadBid(adUnit, new ContextData(), bidResponseRef::set);
    waitForIdleState();
    return bidResponseRef.get();
  }

}
