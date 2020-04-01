package com.criteo.publisher;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoBannerEventControllerTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private CriteoBannerEventController criteoBannerEventController;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteoBannerView criteoBannerView;

  @Mock
  private CriteoBannerAdListener criteoBannerAdListener;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Criteo criteo;

  @Inject
  private Context context;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(criteo.getConfig().getDisplayUrlMacro()).thenReturn("");
    when(criteo.getConfig().getAdTagUrlMode()).thenReturn("");

    when(criteoBannerView.getCriteoBannerAdListener()).thenReturn(criteoBannerAdListener);
    criteoBannerEventController = spy(new CriteoBannerEventController(criteoBannerView, criteo));
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenListener_InvokeItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    doAnswer(answerVoid((CriteoErrorCode ignored) -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      assertTrue(latch.await(1, TimeUnit.SECONDS));
    })).when(criteoBannerAdListener).onAdFailedToReceive(any());

    AdUnit adUnit = mock(AdUnit.class);
    when(criteo.getBidForAdUnit(adUnit)).thenReturn(null);

    runOnMainThreadAndWait(() -> {
      criteoBannerEventController.fetchAdAsync(adUnit);
      latch.countDown();
    });

    waitForIdleState();
  }

  @Test
  public void fetchAdAsyncToken_GivenListener_InvokeItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    doAnswer(answerVoid((CriteoErrorCode ignored) -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      assertTrue(latch.await(1, TimeUnit.SECONDS));
    })).when(criteoBannerAdListener).onAdFailedToReceive(any());

    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    when(criteo.getTokenValue(token, AdUnitType.CRITEO_BANNER)).thenReturn(null);

    runOnMainThreadAndWait(() -> {
      criteoBannerEventController.fetchAdAsync(token);
      latch.countDown();
    });

    waitForIdleState();
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenNoBid_NotifyListenerForFailureAndDoNotDisplayAd()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);

    when(criteo.getBidForAdUnit(adUnit)).thenReturn(null);

    criteoBannerEventController.fetchAdAsync(adUnit);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoBannerEventController, never()).displayAd(any());
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenBid_NotifyListenerForSuccessAndDisplayAd() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    Slot slot = mock(Slot.class);

    when(criteo.getBidForAdUnit(adUnit)).thenReturn(slot);
    when(slot.getDisplayUrl()).thenReturn("http://my.display.url");

    criteoBannerEventController.fetchAdAsync(adUnit);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdReceived(criteoBannerView);
    verify(criteoBannerEventController).displayAd("http://my.display.url");
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenBidTwice_NotifyListenerForSuccessAndDisplayAdTwice() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    Slot slot = mock(Slot.class);

    when(criteo.getBidForAdUnit(adUnit)).thenReturn(slot);
    when(slot.getDisplayUrl())
        .thenReturn("http://my.display.url1")
        .thenReturn("http://my.display.url2");

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(adUnit));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(adUnit));
    waitForIdleState();

    InOrder inOrder = inOrder(criteoBannerAdListener, criteoBannerEventController);
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url1");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView);
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url2");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void fetchAdAsyncToken_GivenNoBid_NotifyListenerForFailureAndDoNotDisplayAd()
      throws Exception {
    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));

    when(criteo.getTokenValue(token, AdUnitType.CRITEO_BANNER)).thenReturn(null);

    criteoBannerEventController.fetchAdAsync(token);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoBannerEventController, never()).displayAd(any());
  }

  @Test
  public void fetchAdAsyncToken_GivenBid_NotifyListenerForSuccessAndDisplayAd() throws Exception {
    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    TokenValue tokenValue = mock(TokenValue.class);

    when(criteo.getTokenValue(token, AdUnitType.CRITEO_BANNER)).thenReturn(tokenValue);
    when(tokenValue.getDisplayUrl()).thenReturn("http://my.display.url");

    criteoBannerEventController.fetchAdAsync(token);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdReceived(criteoBannerView);
    verify(criteoBannerEventController).displayAd("http://my.display.url");
  }

  @Test
  public void fetchAdAsyncToken_GivenBidTwice_NotifyListenerForSuccessAndDisplayAdTwice() throws Exception {
    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    TokenValue tokenValue = mock(TokenValue.class);

    when(criteo.getTokenValue(any(), any())).thenReturn(tokenValue);
    when(tokenValue.getDisplayUrl())
        .thenReturn("http://my.display.url1")
        .thenReturn("http://my.display.url2");

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(token));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(token));
    waitForIdleState();

    InOrder inOrder = inOrder(criteoBannerAdListener, criteoBannerEventController);
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url1");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView);
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url2");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void displayAd_GivenDisplayUrl_LoadItInBanner() throws Exception {
    criteoBannerEventController.displayAd("http://my.display.url");
    waitForIdleState();

    verify(criteoBannerView).loadDataWithBaseURL(any(), any(), any(), any(), any());
  }

  @Test
  public void displayAd_GivenDisplayUrlTwice_LoadItInBannerTwice() throws Exception {
    criteoBannerEventController.displayAd("http://my.display1.url");
    waitForIdleState();

    criteoBannerEventController.displayAd("http://my.display2.url");
    waitForIdleState();

    verify(criteoBannerView, times(2)).loadDataWithBaseURL(any(), any(), any(), any(), any());
  }

  @Test
  public void whenDeeplinkIsLoaded_GivenTargetAppIsNotInstalled_DontThrowActivityNotFound() {
    runOnMainThreadAndWait(() -> {
      WebViewClient webViewClient = criteoBannerEventController.createWebViewClient();
      webViewClient.shouldOverrideUrlLoading(new WebView(context),
          "fake_deeplink://fakeappdispatch");
    });
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }
}
