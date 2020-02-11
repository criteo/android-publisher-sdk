package com.criteo.publisher;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.model.WebViewData;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private CriteoInterstitialEventController criteoInterstitialEventController;

  private WebViewData webViewData;

  @Mock
  private CriteoInterstitialAdListener criteoInterstitialAdListener;

  @Mock
  private CriteoInterstitialAdDisplayListener adDisplayListener;

  @Mock
  private Criteo criteo;

  @Before
  public void setup() throws CriteoInitException {
    MockitoAnnotations.initMocks(this);

    Config config = new Config(InstrumentationRegistry.getContext());
    webViewData = new WebViewData(config);
    webViewData.setContent("html content");

    criteoInterstitialEventController = spy(new CriteoInterstitialEventController(
        criteoInterstitialAdListener,
        adDisplayListener,
        webViewData,
        criteo
    ));
  }

  @Test
  public void testUnload() {
    criteoInterstitialEventController.refresh();

    Assert.assertEquals("", webViewData.getContent());
    Assert.assertEquals(false, webViewData.isLoaded());
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenListener_InvokeItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    doAnswer(answerVoid((CriteoErrorCode ignored) -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      assertTrue(latch.await(1, TimeUnit.SECONDS));
    })).when(criteoInterstitialAdListener).onAdFailedToReceive(any());

    AdUnit adUnit = mock(AdUnit.class);
    when(criteo.getBidForAdUnit(adUnit)).thenReturn(null);

    runOnMainThreadAndWait(() -> {
      criteoInterstitialEventController.fetchAdAsync(adUnit);
      latch.countDown();
    });

    waitForIdleState();
  }

  @Test
  public void fetchAdAsyncInHouse_GivenListener_InvokeItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    doAnswer(answerVoid((CriteoErrorCode ignored) -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      assertTrue(latch.await(1, TimeUnit.SECONDS));
    })).when(criteoInterstitialAdListener).onAdFailedToReceive(any());

    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    when(criteo.getTokenValue(token, AdUnitType.CRITEO_INTERSTITIAL)).thenReturn(null);

    runOnMainThreadAndWait(() -> {
      criteoInterstitialEventController.fetchAdAsync(token);
      latch.countDown();
    });

    waitForIdleState();
  }

  @Test
  public void fetchAdAsyncStandalone_GivenNoBid_NotifyAdListenerForFailure() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    when(criteo.getBidForAdUnit(adUnit)).thenReturn(null);

    criteoInterstitialEventController.fetchAdAsync(adUnit);
    waitForIdleState();

    verify(criteoInterstitialAdListener, never()).onAdReceived();
    verify(criteoInterstitialAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoInterstitialEventController, never()).fetchCreativeAsync(any());
  }

  @Test
  public void fetchAdAsyncStandalone_GivenBid_NotifyAdListenerForSuccessAndFetchCreative()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    Slot slot = mock(Slot.class);

    when(slot.getDisplayUrl()).thenReturn("http://my.creative");
    when(criteo.getBidForAdUnit(adUnit)).thenReturn(slot);

    criteoInterstitialEventController.fetchAdAsync(adUnit);
    waitForIdleState();

    verify(criteoInterstitialAdListener).onAdReceived();
    verify(criteoInterstitialAdListener, never()).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative");
  }

  @Test
  public void fetchAdAsyncInHouse_GivenNoBid_NotifyAdListenerForFailure() throws Exception {
    BidToken bidToken = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    when(criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL)).thenReturn(null);

    criteoInterstitialEventController.fetchAdAsync(bidToken);
    waitForIdleState();

    verify(criteoInterstitialAdListener, never()).onAdReceived();
    verify(criteoInterstitialAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoInterstitialEventController, never()).fetchCreativeAsync(any());
  }

  @Test
  public void fetchAdAsyncInHouse_GivenBid_NotifyAdListenerForSuccessAndFetchCreative()
      throws Exception {
    BidToken bidToken = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    TokenValue tokenValue = mock(TokenValue.class);

    when(tokenValue.getDisplayUrl()).thenReturn("http://my.creative");
    when(criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL)).thenReturn(tokenValue);

    criteoInterstitialEventController.fetchAdAsync(bidToken);
    waitForIdleState();

    verify(criteoInterstitialAdListener).onAdReceived();
    verify(criteoInterstitialAdListener, never()).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative");
  }

  private void waitForIdleState() {
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}