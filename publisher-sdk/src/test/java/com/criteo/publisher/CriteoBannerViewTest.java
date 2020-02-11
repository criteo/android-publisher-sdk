package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.content.Context;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerViewTest {

  @Mock
  private Context context;

  @Mock
  private CriteoBannerEventController controller;

  @Mock
  private Criteo criteo;

  private CriteoBannerView bannerView;

  private BannerAdUnit bannerAdUnit;

  private BidToken bidToken;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    bannerAdUnit = new BannerAdUnit("mock", new AdSize(320, 50));
    bidToken = new BidToken(UUID.randomUUID(), bannerAdUnit);

    bannerView = spy(new CriteoBannerView(context, bannerAdUnit, criteo));
  }

  @Test
  public void loadAdStandalone_GivenController_DelegateToIt() throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();

    bannerView.loadAd();

    verify(controller).fetchAdAsync(bannerAdUnit);
    verifyNoMoreInteractions(controller);
  }

  @Test
  public void loadAdStandalone_GivenNullAdUnitController_DelegateToIt() throws Exception {
    bannerView = spy(new CriteoBannerView(context, null));
    doReturn(controller).when(bannerView).getOrCreateController();

    bannerView.loadAd();

    verify(controller).fetchAdAsync((AdUnit) null);
    verifyNoMoreInteractions(controller);
  }

  @Test
  public void loadAdStandalone_GivenControllerThrowing_DoNotThrow() throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();
    doThrow(RuntimeException.class).when(controller).fetchAdAsync(any(AdUnit.class));

    assertThatCode(bannerView::loadAd).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInStandalone_GivenNonInitializedSdk_DoesNotThrow() throws Exception {
    bannerView = givenBannerUsingNonInitializedSdk();

    assertThatCode(bannerView::loadAd).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInHouse_GivenController_DelegateToIt() throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();

    bannerView.loadAd(bidToken);

    verify(controller).fetchAdAsync(bidToken);
    verifyNoMoreInteractions(controller);
  }

  @Test
  public void loadAdInHouse_GivenNullTokenAndController_DelegateToIt() throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();

    bannerView.loadAd(null);

    verify(controller).fetchAdAsync((BidToken) null);
    verifyNoMoreInteractions(controller);
  }

  @Test
  public void loadAdInHouse_GivenControllerThrowing_DoNotThrow() throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();
    doThrow(RuntimeException.class).when(controller).fetchAdAsync(any(BidToken.class));

    assertThatCode(() -> bannerView.loadAd(bidToken)).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInHouse_GivenNonInitializedSdk_DoesNotThrow() throws Exception {
    bannerView = givenBannerUsingNonInitializedSdk();

    assertThatCode(() -> bannerView.loadAd(bidToken)).doesNotThrowAnyException();
  }

  @Test
  public void loadAdInHouse_GivenTokenWithDifferentButEqualAdUnit_DelegateToController()
      throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();
    BannerAdUnit equalAdUnit = new BannerAdUnit(bannerAdUnit.getAdUnitId(), bannerAdUnit.getSize());
    bidToken = new BidToken(UUID.randomUUID(), equalAdUnit);

    bannerView.loadAd(bidToken);

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
    doReturn(controller).when(bannerView).getOrCreateController();
    BannerAdUnit differentAdUnit = new BannerAdUnit(bannerAdUnit.getAdUnitId() + "_",
        bannerAdUnit.getSize());
    bidToken = new BidToken(UUID.randomUUID(), differentAdUnit);

    bannerView.loadAd(bidToken);

    verifyZeroInteractions(controller);
  }

  /**
   * FIXME See above
   */
  @Test
  public void loadAdInHouse_GivenNotABannerToken_SkipIt() throws Exception {
    doReturn(controller).when(bannerView).getOrCreateController();
    AdUnit differentAdUnit = new InterstitialAdUnit(bannerAdUnit.getAdUnitId());
    bidToken = new BidToken(UUID.randomUUID(), differentAdUnit);

    bannerView.loadAd(bidToken);

    verifyZeroInteractions(controller);
  }

  @Test
  public void getOrCreateController_CalledTwice_ReturnTheSameInstance() throws Exception {
    CriteoBannerEventController controller1 = bannerView.getOrCreateController();
    CriteoBannerEventController controller2 = bannerView.getOrCreateController();

    assertThat(controller1).isSameAs(controller2);
  }

  @Test
  public void new_GivenNonInitializedSdk_DoesNotThrowException() throws Exception {
    givenBannerUsingNonInitializedSdk();

    assertThatCode(() -> new CriteoBannerView(context, bannerAdUnit)).doesNotThrowAnyException();
  }

  private CriteoBannerView givenBannerUsingNonInitializedSdk() {
    Criteo.setInstance(null);
    return new CriteoBannerView(context, bannerAdUnit);
  }

}