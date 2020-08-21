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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Looper;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.AdUnitType;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private CriteoInterstitialEventController criteoInterstitialEventController;

  @Inject
  private Config config;

  @Inject
  private PubSdkApi api;

  private WebViewData webViewData;

  @Mock
  private CriteoInterstitialAdListener criteoInterstitialAdListener;

  @Mock
  private CriteoInterstitialAdDisplayListener adDisplayListener;

  @Mock
  private InterstitialActivityHelper interstitialActivityHelper;

  @Mock
  private Criteo criteo;

  @Before
  public void setup() throws CriteoInitException {
    MockitoAnnotations.initMocks(this);

    webViewData = new WebViewData(config, api);
    webViewData.setContent("html content");

    when(interstitialActivityHelper.isAvailable()).thenReturn(true);

    criteoInterstitialEventController = spy(new CriteoInterstitialEventController(
        criteoInterstitialAdListener,
        adDisplayListener,
        webViewData,
        interstitialActivityHelper,
        criteo
    ));
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
    CdbResponseSlot slot = mock(CdbResponseSlot.class);

    when(slot.getDisplayUrl()).thenReturn("http://my.creative");
    when(criteo.getBidForAdUnit(adUnit)).thenReturn(slot);

    criteoInterstitialEventController.fetchAdAsync(adUnit);
    waitForIdleState();

    verify(criteoInterstitialAdListener).onAdReceived();
    verify(criteoInterstitialAdListener, never()).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative");
  }

  @Test
  public void fetchAdAsyncStandalone_GivenBidTwice_NotifyAdListenerForSuccessAndFetchCreativeTwice()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);

    when(criteo.getBidForAdUnit(adUnit)).thenReturn(slot);
    when(slot.getDisplayUrl())
        .thenReturn("http://my.creative1")
        .thenReturn("http://my.creative2");

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(adUnit));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(adUnit));
    waitForIdleState();

    InOrder inOrder = inOrder(criteoInterstitialAdListener, criteoInterstitialEventController);
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative1");
    inOrder.verify(criteoInterstitialAdListener).onAdReceived();
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative2");
    inOrder.verify(criteoInterstitialAdListener).onAdReceived();
    inOrder.verifyNoMoreInteractions();
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
    DisplayUrlTokenValue tokenValue = mock(DisplayUrlTokenValue.class);

    when(tokenValue.getDisplayUrl()).thenReturn("http://my.creative");
    when(criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL)).thenReturn(tokenValue);

    criteoInterstitialEventController.fetchAdAsync(bidToken);
    waitForIdleState();

    verify(criteoInterstitialAdListener).onAdReceived();
    verify(criteoInterstitialAdListener, never()).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative");
  }

  @Test
  public void fetchAdAsyncInHouse_GivenBidTwice_NotifyAdListenerForSuccessAndFetchCreativeTwice()
      throws Exception {
    BidToken bidToken = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    DisplayUrlTokenValue tokenValue = mock(DisplayUrlTokenValue.class);

    when(criteo.getTokenValue(bidToken, AdUnitType.CRITEO_INTERSTITIAL)).thenReturn(tokenValue);
    when(tokenValue.getDisplayUrl())
        .thenReturn("http://my.creative1")
        .thenReturn("http://my.creative2");

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(bidToken));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(bidToken));
    waitForIdleState();

    InOrder inOrder = inOrder(criteoInterstitialAdListener, criteoInterstitialEventController);
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative1");
    inOrder.verify(criteoInterstitialAdListener).onAdReceived();
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync("http://my.creative2");
    inOrder.verify(criteoInterstitialAdListener).onAdReceived();
    inOrder.verifyNoMoreInteractions();
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}