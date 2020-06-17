package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.StubConstants.STUB_DISPLAY_URL;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.view.WebViewLookup.getRootView;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitial;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.view.WebViewLookup;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
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
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final BannerAdUnit invalidBannerAdUnit = TestAdUnits.BANNER_UNKNOWN;

  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;
  private final InterstitialAdUnit invalidInterstitialAdUnit = TestAdUnits.INTERSTITIAL_UNKNOWN;

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
    context = activityRule.getActivity().getApplicationContext();

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    api = spy(dependencyProvider.providePubSdkApi());
    when(dependencyProvider.providePubSdkApi()).thenReturn(api);
    when(dependencyProvider.provideAndroidUtil()).thenReturn(androidUtil);

    webViewLookup = new WebViewLookup();
  }

  @Test
  public void whenLoadingABanner_GivenBidAvailable_DisplayUrlIsProperlyLoadedInBannerView()
      throws Exception {
    givenInitializedSdk(validBannerAdUnit);

    CriteoBannerView bannerView = whenLoadingABanner(validBannerAdUnit);
    String html = webViewLookup.lookForHtmlContent(bannerView).get();

    assertTrue(STUB_DISPLAY_URL.matcher(html).find());
  }

  @Test
  public void whenLoadingAnInterstitial_GivenBidAvailableAndDeviceInPortrait_DisplayUrlIsProperlyLoadedInInterstitialActivity()
      throws Exception {
    givenDeviceInPortrait();
    whenLoadingAnInterstitial_GivenBidAvailable_DisplayUrlIsProperlyLoadedInInterstitialActivity();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenBidAvailableAndDeviceInLandscape_DisplayUrlIsProperlyLoadedInInterstitialActivity()
      throws Exception {
    givenDeviceInLandscape();
    whenLoadingAnInterstitial_GivenBidAvailable_DisplayUrlIsProperlyLoadedInInterstitialActivity();
  }

  private void whenLoadingAnInterstitial_GivenBidAvailable_DisplayUrlIsProperlyLoadedInInterstitialActivity()
      throws Exception {
    givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitial interstitial = createInterstitial(validInterstitialAdUnit);
    CriteoSync sync = new CriteoSync(interstitial);
    View interstitialView = whenLoadingAndDisplayingAnInterstitial(interstitial, sync);
    String html = webViewLookup.lookForHtmlContent(interstitialView).get();

    assertTrue(html.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void whenLoadingAnInterstitial_GivenBidAvailableTwice_DisplayUrlIsProperlyLoadedInInterstitialActivityTwice()
      throws Exception {
    givenInitializedSdk(validInterstitialAdUnit);

    CriteoInterstitial interstitial = createInterstitial(validInterstitialAdUnit);
    CriteoSync sync = new CriteoSync(interstitial);

    View interstitialView1 = whenLoadingAndDisplayingAnInterstitial(interstitial, sync);
    String html1 = webViewLookup.lookForHtmlContent(interstitialView1).get();

    sync.reset();

    View interstitialView2 = whenLoadingAndDisplayingAnInterstitial(interstitial, sync);
    String html2 = webViewLookup.lookForHtmlContent(interstitialView2).get();

    assertTrue(html1.contains(STUB_CREATIVE_IMAGE));
    assertTrue(html2.contains(STUB_CREATIVE_IMAGE));
  }

  @Test
  public void whenLoadingABanner_GivenNoBidAvailable_NothingIsLoadedInBannerView()
      throws Exception {
    givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerView bannerView = whenLoadingABanner(invalidBannerAdUnit);

    // Empty webview may not be totally empty. When tested, it contains "ul" inside.
    String html = webViewLookup.lookForHtmlContent(bannerView).get();

    assertTrue(html.isEmpty() || html.equals("ul"));
  }

  @Test
  public void whenLoadingAnInterstitial_GivenNoBidAvailable_InterstitialIsNotLoadedAndCannotBeShown()
      throws Exception {
    givenInitializedSdk(invalidInterstitialAdUnit);

    CriteoInterstitial interstitial = whenLoadingAnInterstitial(invalidInterstitialAdUnit);

    assertFalse(interstitial.isAdLoaded());

    Activity activity = webViewLookup.lookForResumedActivity(() -> {
      runOnMainThreadAndWait(interstitial::show);

      activityRule.launchActivity(new Intent(context, DummyActivity.class));
    }).get();

    // So launched activity is not the interstitial one
    assertEquals(DummyActivity.class, activity.getClass());
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

  private View whenLoadingAndDisplayingAnInterstitial(CriteoInterstitial interstitial, CriteoSync sync)
      throws Exception {
    runOnMainThreadAndWait(interstitial::loadAd);
    sync.waitForBid();

    assertTrue(interstitial.isAdLoaded());

    Future<Activity> activity = webViewLookup.lookForResumedActivity(() -> {
      runOnMainThreadAndWait(interstitial::show);
    });

    sync.waitForDisplay();

    return getRootView(activity.get());
  }

  private CriteoInterstitial whenLoadingAnInterstitial(InterstitialAdUnit interstitialAdUnit)
      throws Exception {
    CriteoInterstitial interstitial = createInterstitial(interstitialAdUnit);

    CriteoSync sync = new CriteoSync(interstitial);
    runOnMainThreadAndWait(interstitial::loadAd);
    sync.waitForBid();

    return interstitial;
  }

  private CriteoInterstitial createInterstitial(InterstitialAdUnit adUnit) {
    AtomicReference<CriteoInterstitial> interstitialRef = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      CriteoInterstitial interstitial = new CriteoInterstitial(context, adUnit);
      interstitialRef.set(interstitial);
    });

    return interstitialRef.get();
  }

  @Test
  public void whenLoadingABanner_GivenListenerAndBidAvailable_OnAdReceivedIsCalled()
      throws Exception {
    givenInitializedSdk(validBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBanner(validBannerAdUnit, listener);

    runOnMainThreadAndWait(bannerView::loadAd);
    waitForBids();

    verify(listener).onAdReceived(bannerView);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenLoadingABanner_GivenListenerAndNoBidAvailable_OnAdFailedToReceivedIsCalledWithNoFill()
      throws Exception {
    givenInitializedSdk(invalidBannerAdUnit);

    CriteoBannerAdListener listener = mock(CriteoBannerAdListener.class);
    CriteoBannerView bannerView = createBanner(invalidBannerAdUnit, listener);

    runOnMainThreadAndWait(bannerView::loadAd);
    waitForBids();

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void whenLoadingAnInterstitial_GivenListenerAndNoBidAvailable_OnAdFailedToReceivedIsCalledWithNoFill()
      throws Exception {
    givenInitializedSdk(invalidInterstitialAdUnit);

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial(invalidInterstitialAdUnit, listener);

    runOnMainThreadAndWait(interstitial::loadAd);
    waitForBids();

    verify(listener).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
  }

  private CriteoBannerView createBanner(BannerAdUnit bannerAdUnit,
      CriteoBannerAdListener listener) {
    AtomicReference<CriteoBannerView> bannerViewRef = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      bannerViewRef.set(new CriteoBannerView(context, bannerAdUnit));
      bannerViewRef.get().setCriteoBannerAdListener(listener);
    });

    return bannerViewRef.get();
  }

  private CriteoInterstitial createInterstitial(InterstitialAdUnit interstitialAdUnit,
      CriteoInterstitialAdListener listener) {
    AtomicReference<CriteoInterstitial> interstitial = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      interstitial.set(new CriteoInterstitial(context, interstitialAdUnit));
      interstitial.get().setCriteoInterstitialAdListener(listener);
    });

    return interstitial.get();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenInitializedSdk_ShouldSetInterstitialFlagInTheRequest()
      throws Exception {
    givenInitializedSdk();
    Mockito.clearInvocations(api);

    runOnMainThreadAndWait(() -> {
      CriteoInterstitial interstitial = new CriteoInterstitial(context, validInterstitialAdUnit);
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
  public void whenLoadingAnInterstitial_GivenDeviceInPortrait_NotifyListenerForSuccessOnNextCall()
      throws Exception {
    givenDeviceInPortrait();

    whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall();
  }

  @Test
  public void whenLoadingAnInterstitial_GivenDeviceInLandscape_NotifyListenerForSuccessOnNextCall()
      throws Exception {
    givenDeviceInLandscape();

    whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall();
  }

  private void whenLoadingAnInterstitial_NotifyListenerForSuccessOnNextCall() throws Exception {
    givenInitializedSdk();

    CriteoInterstitialAdListener listener = mock(CriteoInterstitialAdListener.class);
    CriteoInterstitial interstitial = createInterstitial(validInterstitialAdUnit, listener);

    // Given a first bid (that should do a cache miss)
    runOnMainThreadAndWait(interstitial::loadAd);
    waitForBids();

    // Given a second bid (that should success)
    runOnMainThreadAndWait(interstitial::loadAd);
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
    mockedDependenciesRule.waitForIdleState();
  }

  private static final class CriteoSync {

    private final Handler handler;
    private final Runnable init;

    private CountDownLatch isLoaded;
    private CountDownLatch isDisplayed;

    CriteoSync(CriteoBannerView bannerView) {
      this.handler = new Handler(Looper.getMainLooper());
      this.init = () -> {
        this.isLoaded = new CountDownLatch(1);
        this.isDisplayed = isLoaded;
      };
      reset();
      bannerView.setCriteoBannerAdListener(new SyncAdListener());
    }

    CriteoSync(CriteoInterstitial interstitial) {
      this.handler = new Handler(Looper.getMainLooper());
      this.init = () -> {
        this.isLoaded = new CountDownLatch(2);
        this.isDisplayed = new CountDownLatch(1);
      };

      reset();

      SyncAdListener listener = new SyncAdListener();
      interstitial.setCriteoInterstitialAdDisplayListener(listener);
      interstitial.setCriteoInterstitialAdListener(listener);
    }

    /**
     * This method is not atomic. Do not use it on multiple threads.
     */
    void reset() {
      emptyLatches();
      init.run();
    }

    void waitForBid() throws InterruptedException {
      isLoaded.await();
    }

    void waitForDisplay() throws InterruptedException {
      isDisplayed.await();
    }

    private void onLoaded() {
      // Criteo does not seem to totally be ready at this point.
      // It seems to be ready few times after the end of this method.
      // This may be caused by the webview that should load the creative.
      // So we should still wait a little in a non-deterministic way, but not in this method.
      handler.postDelayed(isLoaded::countDown, 1000);
    }

    private void onDisplayed() {
      handler.postDelayed(isDisplayed::countDown, 1000);
    }

    private void onFailed() {
      emptyLatches();
    }

    private void emptyLatches() {
      if (isLoaded != null) {
        while (isLoaded.getCount() > 0) {
          isLoaded.countDown();
        }
      }

      if (isDisplayed != null) {
        while (isDisplayed.getCount() > 0) {
          isDisplayed.countDown();
        }
      }
    }

    private class SyncAdListener implements CriteoBannerAdListener,
        CriteoInterstitialAdListener,
        CriteoInterstitialAdDisplayListener {

      @Override
      public void onAdReceived(View view) {
        onLoaded();
      }

      @Override
      public void onAdReadyToDisplay() {
        onLoaded();
      }

      @Override
      public void onAdReceived() {
        onLoaded();
      }

      @Override
      public void onAdOpened() {
        onDisplayed();
      }

      @Override
      public void onAdFailedToReceive(CriteoErrorCode code) {
        onFailed();
      }

      @Override
      public void onAdFailedToDisplay(CriteoErrorCode error) {
        onFailed();
      }

      @Override
      public void onAdLeftApplication() {
      }

      @Override
      public void onAdClicked() {
      }

      @Override
      public void onAdClosed() {
      }
    }
  }

}
