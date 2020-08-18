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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import androidx.annotation.NonNull;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.WebViewData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialEventControllerTest {

  @Mock
  private CriteoInterstitialAdListener listener;

  @Mock
  private WebViewData webViewData;

  @Mock
  private InterstitialActivityHelper interstitialActivityHelper;

  @Mock
  private Criteo criteo;

  @Mock
  private Context context;

  private CriteoInterstitialEventController controller;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    controller = spy(createController());
  }

  @Test
  public void isAdLoaded_GivenLoadedWebViewData_ReturnTrue() throws Exception {
    givenLoadedWebViewData();

    boolean isLoaded = controller.isAdLoaded();

    assertThat(isLoaded).isTrue();
  }

  @Test
  public void isAdLoaded_GivenNotLoadedWebViewData_ReturnFalse() throws Exception {
    when(webViewData.isLoaded()).thenReturn(false);

    boolean isLoaded = controller.isAdLoaded();

    assertThat(isLoaded).isFalse();
  }

  @Test
  public void show_GivenNotLoadedWebViewData_DoesNothing() throws Exception {
    when(webViewData.isLoaded()).thenReturn(false);

    controller.show();

    verifyNoInteractions(context);
    verifyNoInteractions(listener);
  }

  @Test
  public void show_GivenLoadedWebViewData_NotifyListener() throws Exception {
    givenLoadedWebViewData();

    controller.show();

    verify(listener).onAdOpened();
  }

  @Test
  public void show_GivenLoadedWebViewDataAndNullListener_DoesNotCrash() throws Exception {
    givenLoadedWebViewData();
    listener = null;
    controller = createController();

    assertThatCode(() -> controller.show()).doesNotThrowAnyException();
  }

  @Test
  public void show_GivenLoadedWebViewData_OpenActivity() throws Exception {
    givenLoadedWebViewData("myContent");

    controller.show();

    verify(interstitialActivityHelper).openActivity("myContent", listener);
  }

  @Test
  public void show_GivenLoadedWebViewData_ResetWebViewData() throws Exception {
    givenLoadedWebViewData();

    controller.show();

    verify(webViewData).refresh();
  }

  @Test
  public void fetchAdAsyncStandalone_GivenUnavailableInterstitialActivity_NotifyForFailureWithoutAskingForBid() throws Exception {
    when(interstitialActivityHelper.isAvailable()).thenReturn(false);

    controller.fetchAdAsync(mock(AdUnit.class));

    verify(controller, never()).fetchCreativeAsync(any());
    verify(controller).notifyFor(CriteoListenerCode.INVALID);
    verify(criteo, never()).getBidForAdUnit(any());
  }

  private void givenLoadedWebViewData() {
    givenLoadedWebViewData("ignored");
  }

  private void givenLoadedWebViewData(String webViewContent) {
    when(webViewData.isLoaded()).thenReturn(true);
    when(webViewData.getContent()).thenReturn(webViewContent);
  }

  @NonNull
  private CriteoInterstitialEventController createController() {
    return new CriteoInterstitialEventController(
        listener,
        webViewData,
        interstitialActivityHelper,
        criteo
    );
  }

}