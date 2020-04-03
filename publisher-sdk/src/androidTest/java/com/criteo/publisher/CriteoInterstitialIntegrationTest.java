package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.util.CompletableFuture.completedFuture;
import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.InterstitialAdUnit;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private CriteoInterstitialAdListener listener;

  private Criteo criteo;

  private InterstitialAdUnit interstitialAdUnit = TestAdUnits.INTERSTITIAL;

  private CriteoInterstitial interstitial;

  @Before
  public void setup() throws CriteoInitException {
    MockitoAnnotations.initMocks(this);

    givenInitializedCriteo(interstitialAdUnit);

    interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);
  }

  @Test
  public void loadAdInHouse_GivenSelfMadeToken_NotifyListenerForFailure() throws Exception {
    givenInitializedCriteo(interstitialAdUnit);
    waitForIdleState();

    // This should not be possible since BidToken is not part of the public API.
    // But just in case, we may check that no publisher can attempt this.
    BidToken token = new BidToken(UUID.randomUUID(), interstitialAdUnit);

    interstitial.loadAd(token);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void loadAdForStandaloneTwice_GivenOnlyNoBid_ShouldNotifyListenerTwiceForFailure()
      throws Exception {
    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);

    criteo = mock(Criteo.class, Answers.RETURNS_DEEP_STUBS);
    when(criteo.getBidForAdUnit(interstitialAdUnit)).thenReturn(null);
    when(criteo.getDeviceInfo().getUserAgent()).thenReturn(completedFuture(""));

    CriteoInterstitial interstitial = createInterstitial();
    interstitial.setCriteoInterstitialAdListener(listener);

    runOnMainThreadAndWait(interstitial::loadAd);
    waitForIdleState();

    runOnMainThreadAndWait(interstitial::loadAd);
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
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private CriteoInterstitial createInterstitial() {
    return new CriteoInterstitial(InstrumentationRegistry.getContext(), interstitialAdUnit, criteo);
  }

}