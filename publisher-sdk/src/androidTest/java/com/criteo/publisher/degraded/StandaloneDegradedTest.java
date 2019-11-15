package com.criteo.publisher.degraded;

import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForMockedBid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.network.PubSdkApiHelper;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StandaloneDegradedTest {

  private final BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", new AdSize(1, 2));
  private final InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("interstitial");

  private Context context;

  @Mock
  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DegradedUtil.assumeIsDegraded();

    context = InstrumentationRegistry.getContext();

    CriteoUtil.givenInitializedCriteo();
  }

  @Test
  public void whenLoadingABanner_ShouldNotDoAnyCallToCdb() throws Exception {
    PubSdkApiHelper.withApi(api, () -> {
      runOnMainThreadAndWait(() -> {
        CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);

        bannerView.loadAd();
      });
    });

    waitForMockedBid();

    verifyZeroInteractions(api);
  }

  @Test
  public void whenLoadingTwiceABanner_ShouldCallBackListenerWithErrorNoFill() throws Exception {
    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    AtomicReference<CriteoBannerView> bannerView = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      bannerView.set(new CriteoBannerView(context, bannerAdUnit));
    });

    bannerView.get().setCriteoBannerAdListener(listener);

    runOnMainThreadAndWait(bannerView.get()::loadAd);
    waitForMockedBid();

    // Load twice, because first one is a cache miss
    runOnMainThreadAndWait(bannerView.get()::loadAd);
    waitForMockedBid();

    verify(listener, never()).onAdReceived(any());
    verify(listener, times(2)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

  @Test
  public void whenLoadingAnInterstitial_ShouldNotDoAnyCallToCdb() throws Exception {
    PubSdkApiHelper.withApi(api, () -> {
      runOnMainThreadAndWait(() -> {
        CriteoInterstitial interstitial = new CriteoInterstitial(context, interstitialAdUnit);

        interstitial.loadAd();
      });
    });

    waitForMockedBid();

    verifyZeroInteractions(api);
  }

  @Test
  public void whenLoadingTwiceAnInterstitial_ShouldCallBackListenerWithErrorNoFill() throws Exception {
    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    AtomicReference<CriteoInterstitial> interstitial = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      interstitial.set(new CriteoInterstitial(context, interstitialAdUnit));
    });

    interstitial.get().setCriteoInterstitialAdListener(listener);

    runOnMainThreadAndWait(interstitial.get()::loadAd);
    waitForMockedBid();

    // Load twice, because first one is a cache miss
    runOnMainThreadAndWait(interstitial.get()::loadAd);
    waitForMockedBid();

    verify(listener, never()).onAdReceived();
    verify(listener, times(2)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
  }

}
