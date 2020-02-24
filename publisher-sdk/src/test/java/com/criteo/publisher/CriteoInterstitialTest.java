package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.content.Context;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialTest {

  @Mock
  private Context context;

  @Mock
  private Criteo criteo;

  private InterstitialAdUnit adUnit = new InterstitialAdUnit("mock");

  private CriteoInterstitial interstitial;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    interstitial = spy(new CriteoInterstitial(context, adUnit, criteo));
  }

  @Test
  public void loadAdStandalone_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();

    interstitial.loadAd();
    interstitial.loadAd();

    verify(controller, times(2)).fetchAdAsync(adUnit);
  }

  @Test
  public void loadAdInHouse_GivenControllerAndLoadTwice_DelegateToItTwice() throws Exception {
    BidToken token = new BidToken(UUID.randomUUID(), adUnit);
    CriteoInterstitialEventController controller = givenMockedController();

    interstitial.loadAd(token);
    interstitial.loadAd(token);

    verify(controller, times(2)).fetchAdAsync(token);
  }

  @Test
  public void loadAdInHouse_GivenTokenWithDifferentButEqualAdUnit_DelegateToController()
      throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    InterstitialAdUnit equalAdUnit = new InterstitialAdUnit(adUnit.getAdUnitId());
    BidToken bidToken = new BidToken(UUID.randomUUID(), equalAdUnit);

    interstitial.loadAd(bidToken);

    verify(controller).fetchAdAsync(bidToken);
    verifyNoMoreInteractions(controller);
  }

  /**
   * FIXME EE-831 this test scenario is not well-defined. This is currently failing silently. Should
   * the failure be logged and where ? Should the listener be notified for the failure ? This is an
   * integration error on publisher side, at least when an interstitial (or native) token is
   * provided for a banner view. But, for banner token, this is also due to an API that is error
   * prone: The ad unit given to the view is useless for in-house.
   */
  @Test
  public void loadAdInHouse_GivenTokenWithDifferentAdUnit_SkipIt() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    InterstitialAdUnit differentAdUnit = new InterstitialAdUnit(adUnit.getAdUnitId() + "_");
    BidToken bidToken = new BidToken(UUID.randomUUID(), differentAdUnit);

    interstitial.loadAd(bidToken);

    verifyZeroInteractions(controller);
  }

  /**
   * FIXME See above
   */
  @Test
  public void loadAdInHouse_GivenNotABannerToken_SkipIt() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    AdUnit differentAdUnit = new NativeAdUnit(adUnit.getAdUnitId());
    BidToken bidToken = new BidToken(UUID.randomUUID(), differentAdUnit);

    interstitial.loadAd(bidToken);

    verifyZeroInteractions(controller);
  }

  @Test
  public void show_GivenController_DelegateToIt() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();

    interstitial.show();

    verify(controller).show(context);
  }

  @Test
  public void show_GivenThrowingController_DoesNotThrow() throws Exception {
    CriteoInterstitialEventController controller = givenMockedController();
    doThrow(RuntimeException.class).when(controller).show(any());

    assertThatCode(interstitial::show).doesNotThrowAnyException();
  }

  private CriteoInterstitialEventController givenMockedController() {
    CriteoInterstitialEventController controller = mock(CriteoInterstitialEventController.class);
    doReturn(controller).when(interstitial).getOrCreateController();
    return controller;
  }

}