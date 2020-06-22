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

package com.criteo.publisher.util;

import static com.criteo.publisher.util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.util.CriteoResultReceiver.ACTION_LEFT_CLICKED;
import static com.criteo.publisher.util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.os.Bundle;
import android.os.Handler;
import com.criteo.publisher.CriteoInterstitialAdListener;
import java.lang.ref.WeakReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoResultReceiverTest {

  private CriteoResultReceiver criteoResultReceiver;

  @Mock
  private WeakReference<CriteoInterstitialAdListener> criteoInterstitialAdListenerRef;

  @Mock
  private CriteoInterstitialAdListener criteoInterstitialAdListener;

  private Bundle bundle;

  @Mock
  private Handler handler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    criteoResultReceiver = new CriteoResultReceiver(
        handler,
        criteoInterstitialAdListenerRef
    );
  }

  @Test
  public void givenListenerReachable_whenSendOnClick_ThenCallbackShouldBeCalled() {
    givenReachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_LEFT_CLICKED));

    verify(criteoInterstitialAdListener, times(1)).onAdLeftApplication();
    verify(criteoInterstitialAdListener, times(1)).onAdClicked();
  }

  @Test
  public void givenListenerReachable_whenSendOnClosed_ThenCallbackShouldBeCalled() {
    givenReachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_CLOSED));

    verify(criteoInterstitialAdListener, times(1)).onAdClosed();
  }

  @Test
  public void givenListenerUnreachable_whenSendOnClick_ThenCallbackShouldNotBeCalled() {
    givenUnreachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_LEFT_CLICKED));
    verifyNoInteractions(criteoInterstitialAdListener);
  }

  @Test
  public void givenListenerUnreachable_whenSendOnClose_ThenCallbackShouldNotBeCalled() {
    givenUnreachableListenerReference();

    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_CLOSED));

    verify(criteoInterstitialAdListener, never()).onAdClosed();
  }

  private Bundle getActionBundle(int actionClosed) {
    bundle = new Bundle();
    bundle.putInt(INTERSTITIAL_ACTION, actionClosed);
    return bundle;
  }

  private void givenReachableListenerReference() {
    when(criteoInterstitialAdListenerRef.get()).thenReturn(criteoInterstitialAdListener);
  }

  private void givenUnreachableListenerReference() {
    when(criteoInterstitialAdListenerRef.get()).thenReturn(null);
  }
}