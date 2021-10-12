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

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.util.CompletableFuture.completedFuture;
import static org.junit.Assert.assertFalse;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CriteoInterstitialIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private CriteoInterstitialAdListener listener;

  private Criteo criteo;

  private InterstitialAdUnit interstitialAdUnit = TestAdUnits.INTERSTITIAL;

  private CriteoInterstitial interstitial;

  @Before
  public void setup() throws CriteoInitException {
    givenInitializedCriteo(interstitialAdUnit);

    interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);
  }

  @Test
  public void loadAdInHouse_GivenSelfMadeToken_NotifyListenerForFailure() throws Exception {
    givenInitializedCriteo(interstitialAdUnit);
    waitForIdleState();

    // This should not be possible since BidResponse constructor is not part of the public API.
    // But just in case, we may check that no publisher can attempt this.
    Bid bid = mock(Bid.class);

    interstitial.loadAd(bid);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void loadAdForStandaloneTwice_GivenOnlyNoBid_ShouldNotifyListenerTwiceForFailure()
      throws Exception {
    ContextData contextData = mock(ContextData.class);
    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);

    criteo = mock(Criteo.class, Answers.RETURNS_DEEP_STUBS);
    givenMockedNoBidResponse(interstitialAdUnit, contextData);

    when(criteo.getDeviceInfo().getUserAgent()).thenReturn(completedFuture(""));

    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    runOnMainThreadAndWait(() -> interstitial.loadAd(new ContextData()));
    waitForIdleState();

    runOnMainThreadAndWait(() -> interstitial.loadAd(new ContextData()));
    waitForIdleState();

    verify(listener, times(2)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

  @Test
  public void isAdLoaded_GivenNewInstance_ReturnFalse() throws Exception {
    CriteoInterstitial interstitial = createInterstitial();

    boolean isAdLoaded = interstitial.isAdLoaded();

    assertFalse(isAdLoaded);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

  private CriteoInterstitial createInterstitial() {
    return new CriteoInterstitial(interstitialAdUnit, criteo);
  }


  private void givenMockedNoBidResponse(AdUnit adUnit, ContextData contextData) {
    doAnswer(answerVoid((AdUnit ignored, BidListener bidListener) -> bidListener
        .onNoBid()))
        .when(criteo)
        .getBidForAdUnit(eq(adUnit), eq(contextData), any(BidListener.class));
  }
}