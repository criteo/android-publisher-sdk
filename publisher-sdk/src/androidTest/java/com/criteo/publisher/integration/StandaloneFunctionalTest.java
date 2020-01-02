package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_DISPLAY_URL;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.WebViewLookup;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StandaloneFunctionalTest {

  private static final Charset CHARSET = StandardCharsets.UTF_8;

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;

  private final InterstitialAdUnit interstitialAdUnit = TestAdUnits.INTERSTITIAL;

  private PubSdkApi api;

  @Mock
  private AndroidUtil androidUtil;

  private Context context;

  @Captor
  private ArgumentCaptor<CdbRequest> requestCaptor;

  private WebViewLookup webViewLookup;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    context = InstrumentationRegistry.getContext();

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    api = spy(dependencyProvider.providePubSdkApi(context));
    when(dependencyProvider.providePubSdkApi(any())).thenReturn(api);
    when(dependencyProvider.provideAndroidUtil(any())).thenReturn(androidUtil);

    webViewLookup = new WebViewLookup();
  }

  @Test
  public void whenLoadingABanner_GivenBidAvailable_DisplayUrlIsProperlyLoadedInBannerView() throws Exception {
    givenInitializedSdk(validBannerAdUnit);

    CriteoBannerView bannerView = whenLoadingABanner(validBannerAdUnit);
    String html = webViewLookup.lookForHtmlContent(bannerView).get();

    assertTrue(html.contains(STUB_DISPLAY_URL));
  }

  @Test
  public void whenLoadingABanner_GivenNoBidAvailable_NothingIsLoadedInBannerView() throws Exception {
    givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerView bannerView = whenLoadingABanner(invalidBannerAdUnit);

    // Empty webview may not be totally empty. When tested, it contains "ul" inside.
    // So instead of testing that HTML is empty, we could test that no resources in fetched.
    String html = webViewLookup.lookForHtmlContent(bannerView).get();
    List<String> loadedResources = webViewLookup.lookForLoadedResources(html, CHARSET).get();

    assertTrue(loadedResources.isEmpty());
  }

  private CriteoBannerView whenLoadingABanner(BannerAdUnit bannerAdUnit) throws Exception {
    AtomicReference<CriteoBannerView> bannerViewRef = new AtomicReference<>();
    AtomicReference<CriteoSync> syncRef = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      CriteoBannerView bannerView = new CriteoBannerView(context, bannerAdUnit);
      syncRef.set(new CriteoSync(bannerView));
      bannerViewRef.set(bannerView);

      bannerView.loadAd();
    });

    syncRef.get().waitForBid();
    return bannerViewRef.get();
  }

  @Test
  public void whenLoadingABanner_GivenListenerAndBidAvailable_OnAdReceivedIsCalled() throws Exception {
    givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBanner(validBannerAdUnit, listener);

    runOnMainThreadAndWait(bannerView::loadAd);
    waitForBids();

    verify(listener).onAdReceived(bannerView);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenLoadingABanner_GivenListenerAndNoBidAvailable_OnAdFailedToReceivedIsCalledWithNoFill() throws Exception {
    givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBanner(invalidBannerAdUnit, listener);

    runOnMainThreadAndWait(bannerView::loadAd);
    waitForBids();

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
  }

  private CriteoBannerView createBanner(BannerAdUnit bannerAdUnit, CriteoBannerAdListener listener) {
    AtomicReference<CriteoBannerView> bannerViewRef = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      bannerViewRef.set(new CriteoBannerView(context, bannerAdUnit));
      bannerViewRef.get().setCriteoBannerAdListener(listener);
    });

    return bannerViewRef.get();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenInitializedSdk_ShouldSetInterstitialFlagInTheRequest()
      throws Exception {
    givenInitializedSdk();
    Mockito.clearInvocations(api);

    runOnMainThreadAndWait(() -> {
      CriteoInterstitial interstitial = new CriteoInterstitial(context, interstitialAdUnit);
      interstitial.loadAd();
    });
    waitForBids();

    verify(api).loadCdb(requestCaptor.capture(), anyString());
    CdbRequest request = requestCaptor.getValue();

    boolean interstitialFlag = request.toJson()
        .getJSONArray("slots")
        .getJSONObject(0)
        .getBoolean("interstitial");

    assertTrue(interstitialFlag);
  }

  @Test
  public void whenLoadingAnInterstitial_GivenDeviceInPortrait_NotifyListenerForSuccessOnNextCall() throws Exception {
    givenDeviceInPortrait();

    whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenDeviceInLandscape_NotifyListenerForSuccessOnNextCall() throws Exception {
    givenDeviceInLandscape();

    whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall();
  }

  private void whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall() throws Exception {
    givenInitializedSdk();

    AtomicReference<CriteoInterstitial> interstitial = new AtomicReference<>();
    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);

    runOnMainThreadAndWait(() -> {
      interstitial.set(new CriteoInterstitial(context, interstitialAdUnit));
      interstitial.get().setCriteoInterstitialAdListener(listener);
    });

    // Given a first bid (that should do a cache miss)
    runOnMainThreadAndWait(interstitial.get()::loadAd);
    waitForBids();

    // Given a second bid (that should success)
    runOnMainThreadAndWait(interstitial.get()::loadAd);
    waitForBids();

    InOrder inOrder = inOrder(listener);
    inOrder.verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    inOrder.verify(listener).onAdReceived();
    inOrder.verifyNoMoreInteractions();
  }

  private void givenDeviceInPortrait() {
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_PORTRAIT);
  }

  private void givenDeviceInLandscape() {
    when(androidUtil.getOrientation()).thenReturn(Configuration.ORIENTATION_LANDSCAPE);
  }

  private void givenInitializedSdk(AdUnit... preloadedAdUnits) throws Exception {
    givenInitializedCriteo(preloadedAdUnits);
    waitForBids();
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private static final class CriteoSync {

    private final CountDownLatch isLoaded;
    private final Handler handler;

    CriteoSync(CriteoBannerView bannerView) {
      this.isLoaded = new CountDownLatch(1);
      this.handler = new Handler(Looper.getMainLooper());
      bannerView.setCriteoBannerAdListener(new SyncAdListener());
    }

    void waitForBid() throws InterruptedException {
      isLoaded.await();
    }

    private void onLoaded() {
      // Criteo does not seem to totally be ready at this point.
      // It seems to be ready few times after the end of this method.
      // This may be caused by the webview that should load the creative.
      // So we should still wait a little in a non-deterministic way, but not in this method.
      handler.postDelayed(isLoaded::countDown, 1000);
    }

    private void onFailed() {
      isLoaded.countDown();
    }

    private class SyncAdListener implements CriteoBannerAdListener {
      @Override
      public void onAdReceived(View view) {
        onLoaded();
      }

      @Override
      public void onAdFailedToReceive(CriteoErrorCode code) {
        onFailed();
      }

      @Override
      public void onAdLeftApplication() {
      }

      @Override
      public void onAdClicked() {
      }

      @Override
      public void onAdOpened() {
      }

      @Override
      public void onAdClosed() {
      }
    }
  }

}
