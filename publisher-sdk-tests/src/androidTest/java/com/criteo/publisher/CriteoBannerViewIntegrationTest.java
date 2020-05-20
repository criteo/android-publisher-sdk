package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerViewIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private CriteoBannerAdListener criteoBannerAdListener;

  private CriteoBannerView criteoBannerView;

  private BannerAdUnit bannerAdUnit = TestAdUnits.BANNER_320_50;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    runOnMainThreadAndWait(() -> {
      criteoBannerView = new CriteoBannerView(
          InstrumentationRegistry.getContext(),
          bannerAdUnit);
    });

    criteoBannerView.setCriteoBannerAdListener(criteoBannerAdListener);
  }

  @Test
  public void loadAdInHouse_GivenSelfMadeToken_NotifyListenerForFailure() throws Exception {
    givenInitializedCriteo(bannerAdUnit);
    waitForIdleState();

    // This should not be possible since BidToken is not part of the public API.
    // But just in case, we may check that no publisher can attempt this.
    BidToken token = new BidToken(UUID.randomUUID(), bannerAdUnit);

    criteoBannerView.loadAd(token);
    waitForIdleState();

    verify(criteoBannerAdListener, never()).onAdReceived(criteoBannerView);
    verify(criteoBannerAdListener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}