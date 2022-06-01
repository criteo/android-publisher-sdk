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

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NETWORK_ERROR;
import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor;
import com.criteo.publisher.config.Config;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

public class CriteoInterstitialEventControllerIntegrationTest {

  private static final String GOOD_DISPLAY_URL = "http://good.display/url";
  private static final String GOOD_CREATIVE = "dummy creative";
  private static final String BAD_DISPLAY_URL = "http://bad.display/url";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private CriteoInterstitialEventController criteoInterstitialEventController;

  @SpyBean
  private Config config;

  @SpyBean
  private PubSdkApi api;

  private WebViewData webViewData;

  @Mock
  private CriteoInterstitial interstitial;

  @Mock
  private CriteoInterstitialAdListener listener;

  @Mock
  private InterstitialActivityHelper interstitialActivityHelper;

  @Mock
  private Criteo criteo;

  @Inject
  private DeviceInfo deviceInfo;

  private final DirectMockRunOnUiThreadExecutor runOnUiThreadExecutor = new DirectMockRunOnUiThreadExecutor();

  @Before
  public void setup() throws Exception {
    webViewData = new WebViewData(config, api);

    when(interstitialActivityHelper.isAvailable()).thenReturn(true);
    when(criteo.getDeviceInfo()).thenReturn(deviceInfo);

    when(mockedDependenciesRule.getDependencyProvider().provideRunOnUiThreadExecutor()).thenReturn(
        runOnUiThreadExecutor);

    Answer<?> checkIsRunningOnUiThread = invocation -> {
      runOnUiThreadExecutor.expectIsRunningInExecutor();
      return null;
    };
    doAnswer(checkIsRunningOnUiThread).when(listener).onAdReceived(any());
    doAnswer(checkIsRunningOnUiThread).when(listener).onAdFailedToReceive(any());

    doAnswer(invocation -> new ByteArrayInputStream(GOOD_CREATIVE.getBytes())).when(api)
        .executeRawGet(eq(new URL(GOOD_DISPLAY_URL)), any());
    doThrow(IOException.class).when(api).executeRawGet(eq(new URL(BAD_DISPLAY_URL)), any());

    String adTagDataMacro = config.getAdTagDataMacro();
    when(config.getAdTagDataMode()).thenReturn(adTagDataMacro);

    InterstitialListenerNotifier listenerNotifier = new InterstitialListenerNotifier(
        interstitial,
        listener,
        runOnUiThreadExecutor
    );

    criteoInterstitialEventController = spy(new CriteoInterstitialEventController(
        webViewData,
        interstitialActivityHelper,
        criteo,
        listenerNotifier
    ));
  }

  @After
  public void tearDown() throws Exception {
    runOnUiThreadExecutor.verifyExpectations();
  }

  @Test
  public void fetchAdAsyncStandalone_GivenNoBid_NotifyAdListenerForFailure() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    givenMockedNoBidResponse(adUnit, contextData);

    criteoInterstitialEventController.fetchAdAsync(adUnit, contextData);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController, never()).fetchCreativeAsync(any());
  }

  @Test
  public void fetchAdAsyncStandalone_GivenBidAndGoodDisplayUrl_FetchCreativeAndNotifyListenerForSuccess()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    when(slot.getDisplayUrl()).thenReturn(GOOD_DISPLAY_URL);
    givenMockedBidResponse(adUnit, contextData, slot);

    criteoInterstitialEventController.fetchAdAsync(adUnit, contextData);
    waitForIdleState();

    assertThat(criteoInterstitialEventController.isAdLoaded()).isTrue();
    assertThat(webViewData.getContent()).isEqualTo(GOOD_CREATIVE);
    verify(listener).onAdReceived(interstitial);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController).fetchCreativeAsync(GOOD_DISPLAY_URL);
  }

  @Test
  public void fetchAdAsyncStandalone_GivenBidAndBadDisplayUrl_NotifyListenerForFailureToDisplay()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    when(slot.getDisplayUrl()).thenReturn(BAD_DISPLAY_URL);
    givenMockedBidResponse(adUnit, contextData, slot);

    criteoInterstitialEventController.fetchAdAsync(adUnit, contextData);
    waitForIdleState();

    assertThat(criteoInterstitialEventController.isAdLoaded()).isFalse();
    assertThat(webViewData.getContent()).isEmpty();
    verify(listener).onAdFailedToReceive(ERROR_CODE_NETWORK_ERROR);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController).fetchCreativeAsync(BAD_DISPLAY_URL);
  }

  @Test
  public void fetchAdAsyncStandalone_GivenBidTwice_FetchCreativeTwiceAndNotifyListenerIfSuccess()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    CdbResponseSlot slot = mock(CdbResponseSlot.class);
    givenMockedBidResponse(adUnit, contextData, slot);

    when(slot.getDisplayUrl())
        .thenReturn(GOOD_DISPLAY_URL)
        .thenReturn(BAD_DISPLAY_URL);

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(adUnit, contextData));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(adUnit, contextData));
    waitForIdleState();

    InOrder inOrder = inOrder(listener, criteoInterstitialEventController);
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync(GOOD_DISPLAY_URL);
    inOrder.verify(listener).onAdReceived(interstitial);
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync(BAD_DISPLAY_URL);
    inOrder.verify(listener).onAdFailedToReceive(ERROR_CODE_NETWORK_ERROR);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void fetchAdAsyncInHouse_GivenNoBid_NotifyAdListenerForFailure() throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(any())).thenReturn(null);

    criteoInterstitialEventController.fetchAdAsync(bid);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController, never()).fetchCreativeAsync(any());
  }

  @Test
  public void fetchAdAsyncInHouse_GivenNillBid_NotifyAdListenerForFailure() throws Exception {
    criteoInterstitialEventController.fetchAdAsync((Bid) null);
    waitForIdleState();

    verify(listener).onAdFailedToReceive(ERROR_CODE_NO_FILL);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController, never()).fetchCreativeAsync(any());
  }

  @Test
  public void fetchAdAsyncInHouse_GivenBidAndGoodDisplayUrl_FetchCreativeAndNotifyListenerForSuccess()
      throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(CRITEO_INTERSTITIAL)).thenReturn(GOOD_DISPLAY_URL);

    criteoInterstitialEventController.fetchAdAsync(bid);
    waitForIdleState();

    assertThat(criteoInterstitialEventController.isAdLoaded()).isTrue();
    assertThat(webViewData.getContent()).isEqualTo(GOOD_CREATIVE);
    verify(listener).onAdReceived(interstitial);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController).fetchCreativeAsync(GOOD_DISPLAY_URL);
  }

  @Test
  public void fetchAdAsyncInHouse_GivenBidAndBadDisplayUrl_NotifyListenerForFailureToDisplay()
      throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(CRITEO_INTERSTITIAL)).thenReturn(BAD_DISPLAY_URL);

    criteoInterstitialEventController.fetchAdAsync(bid);
    waitForIdleState();

    assertThat(criteoInterstitialEventController.isAdLoaded()).isFalse();
    assertThat(webViewData.getContent()).isEmpty();
    verify(listener).onAdFailedToReceive(ERROR_CODE_NETWORK_ERROR);
    verifyNoMoreInteractions(listener);
    verify(criteoInterstitialEventController).fetchCreativeAsync(BAD_DISPLAY_URL);
  }

  @Test
  public void fetchAdAsyncInHouse_GivenBidTwice_FetchCreativeTwiceAndNotifyListenerIfSuccess()
      throws Exception {
    Bid bid = mock(Bid.class);
    when(bid.consumeDisplayUrlFor(CRITEO_INTERSTITIAL))
        .thenReturn(GOOD_DISPLAY_URL)
        .thenReturn(BAD_DISPLAY_URL);

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(bid));
    waitForIdleState();

    runOnMainThreadAndWait(() -> criteoInterstitialEventController.fetchAdAsync(bid));
    waitForIdleState();

    InOrder inOrder = inOrder(listener, criteoInterstitialEventController);
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync(GOOD_DISPLAY_URL);
    inOrder.verify(listener).onAdReceived(interstitial);
    inOrder.verify(criteoInterstitialEventController).fetchCreativeAsync(BAD_DISPLAY_URL);
    inOrder.verify(listener).onAdFailedToReceive(ERROR_CODE_NETWORK_ERROR);
    inOrder.verifyNoMoreInteractions();
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
    doAnswer(answerVoid((AdUnit adUnit1, ContextData ignored2, BidListener bidListener) -> bidListener
        .onNoBid()))
        .when(criteo)
        .getBidForAdUnit(eq(adUnit), eq(contextData), any(BidListener.class));
  }
}