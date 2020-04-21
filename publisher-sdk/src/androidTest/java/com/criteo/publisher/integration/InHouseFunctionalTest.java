package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.support.annotation.NonNull;
import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;

public class InHouseFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;

  @Inject
  private Context context;

  @Test
  public void loadBannerAd_GivenValidAdUnit_ThenListenerIsNotifiedOfTheSuccess() throws Exception {
    Criteo criteo = givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView(validBannerAdUnit);
    bannerView.setCriteoBannerAdListener(listener);

    BidResponse bidResponse = criteo.getBidResponse(validBannerAdUnit);
    bannerView.loadAd(bidResponse.getBidToken());
    waitForIdleState();

    verify(listener).onAdReceived(bannerView);
  }

  @Test
  public void loadBannerAd_GivenInvalidAdUnit_ThenListenerIsNotifiedOfTheFailure()
      throws Exception {
    Criteo criteo = givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBannerView(invalidBannerAdUnit);
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
    CriteoBannerView bannerView = createBannerView(validBannerAdUnit);
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

  @NonNull
  private CriteoBannerView createBannerView(BannerAdUnit bannerAdUnit) {
    AtomicReference<CriteoBannerView> bannerViewRef = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      bannerViewRef.set(bannerView);
    });

    return bannerViewRef.get();
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
