/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CriteoBannerEventControllerTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private CriteoBannerEventController criteoBannerEventController;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CriteoBannerAdWebView criteoBannerView;

  @Mock
  private CriteoBannerAdListener criteoBannerAdListener;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Criteo criteo;

  @Inject
  private Context context;

  @Inject
  private TopActivityFinder topActivityFinder;

  @Inject
  private RunOnUiThreadExecutor runOnUiThreadExecutor;

  @Before
  public void setUp() throws Exception {
    when(criteo.getConfig().getDisplayUrlMacro()).thenReturn("");
    when(criteo.getConfig().getAdTagUrlMode()).thenReturn("");

    when(criteoBannerView.getCriteoBannerAdListener()).thenReturn(criteoBannerAdListener);
    criteoBannerEventController = spy(new CriteoBannerEventController(
        criteoBannerView,
        criteo,
        topActivityFinder,
        runOnUiThreadExecutor
    ));
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenListener_InvokeItAsyncOnMainThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    doAnswer(answerVoid((CriteoErrorCode ignored) -> {
      assertSame(Thread.currentThread(), Looper.getMainLooper().getThread());
      assertTrue(latch.await(1, TimeUnit.SECONDS));
    })).when(criteoBannerAdListener).onAdFailedToReceive(any());

    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    givenMockedNoBidResponse(adUnit, contextData);

    runOnMainThreadAndWait(() -> {
      criteoBannerEventController.fetchAdAsync(adUnit, contextData);
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

    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(CRITEO_BANNER)).thenReturn(null);

    runOnMainThreadAndWait(() -> {
      criteoBannerEventController.fetchAdAsync(bid);
      latch.countDown();
    });

    waitForIdleState();
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenNoBid_NotifyListenerForFailureAndDoNotDisplayAd()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    givenMockedNoBidResponse(adUnit, contextData);

    criteoBannerEventController.fetchAdAsync(adUnit, contextData);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoBannerEventController, never()).displayAd(any());
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenBid_NotifyListenerForSuccessAndDisplayAd() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    givenMockedBidResponse(adUnit, contextData, slot);

    when(slot.getDisplayUrl()).thenReturn("http://my.display.url");

    criteoBannerEventController.fetchAdAsync(adUnit, contextData);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdReceived(criteoBannerView.getParentContainer());
    verify(criteoBannerEventController).displayAd("http://my.display.url");
  }

  @Test
  public void fetchAdAsyncAdUnit_GivenBidTwice_NotifyListenerForSuccessAndDisplayAdTwice()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    givenMockedBidResponse(adUnit, contextData, slot);

    when(slot.getDisplayUrl())
        .thenReturn("http://my.display.url1")
        .thenReturn("http://my.display.url2");

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(adUnit, contextData));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(adUnit, contextData));
    waitForIdleState();

    InOrder inOrder = inOrder(criteoBannerAdListener, criteoBannerEventController);
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url1");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView.getParentContainer());
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url2");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView.getParentContainer());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void fetchAdAsyncToken_GivenNoBid_NotifyListenerForFailureAndDoNotDisplayAd()
      throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(any())).thenReturn(null);

    criteoBannerEventController.fetchAdAsync(bid);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoBannerEventController, never()).displayAd(any());
  }

  @Test
  public void fetchAdAsyncToken_GivenNullBid_NotifyListenerForFailureAndDoNotDisplayAd() throws Exception {
    criteoBannerEventController.fetchAdAsync((Bid) null);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoBannerEventController, never()).displayAd(any());
  }

  @Test
  public void fetchAdAsyncToken_GivenBid_NotifyListenerForSuccessAndDisplayAd() throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(CRITEO_BANNER)).thenReturn("http://my.display.url");

    criteoBannerEventController.fetchAdAsync(bid);
    waitForIdleState();

    verify(criteoBannerAdListener).onAdReceived(criteoBannerView.getParentContainer());
    verify(criteoBannerEventController).displayAd("http://my.display.url");
  }

  @Test
  public void fetchAdAsyncToken_GivenBidTwice_NotifyListenerForSuccessAndDisplayAdTwice()
      throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(CRITEO_BANNER))
        .thenReturn("http://my.display.url1")
        .thenReturn("http://my.display.url2");

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(bid));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoBannerEventController.fetchAdAsync(bid));
    waitForIdleState();

    InOrder inOrder = inOrder(criteoBannerAdListener, criteoBannerEventController);
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url1");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView.getParentContainer());
    inOrder.verify(criteoBannerEventController).displayAd("http://my.display.url2");
    inOrder.verify(criteoBannerAdListener).onAdReceived(criteoBannerView.getParentContainer());
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
      webViewClient.shouldOverrideUrlLoading(
          new WebView(context),
          "fake_deeplink://fakeappdispatch"
      );
    });
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

  private void givenMockedBidResponse(
      AdUnit adUnit,
      ContextData contextData,
      CdbResponseSlot cdbResponseSlot
  ) {
    doAnswer(answerVoid((AdUnit ignored, ContextData ignored2, BidListener bidListener) -> bidListener
        .onBidResponse(cdbResponseSlot)))
        .when(criteo)
        .getBidForAdUnit(eq(adUnit), eq(contextData), any(BidListener.class));
  }

  private void givenMockedNoBidResponse(AdUnit adUnit, ContextData contextData) {
    doAnswer(answerVoid((AdUnit ignored, ContextData ignored2, BidListener bidListener) -> bidListener
        .onNoBid()))
        .when(criteo)
        .getBidForAdUnit(eq(adUnit), eq(contextData), any(BidListener.class));
  }
}
