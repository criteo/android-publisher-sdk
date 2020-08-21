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
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.os.Handler;
import com.criteo.publisher.CriteoListenerCode;
import com.criteo.publisher.tasks.InterstitialListenerNotifier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoResultReceiverTest {

  private CriteoResultReceiver criteoResultReceiver;

  @Mock
  private InterstitialListenerNotifier listenerNotifier;

  @Mock
  private Handler handler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    criteoResultReceiver = new CriteoResultReceiver(
        handler,
        listenerNotifier
    );
  }

  @Test
  public void givenListenerNotifier_whenSendOnClick_ThenCallbackShouldBeCalled() {
    criteoResultReceiver.onReceiveResult(
        RESULT_CODE_SUCCESSFUL,
        getActionBundle(ACTION_LEFT_CLICKED)
    );

    verify(listenerNotifier).notifyFor(CriteoListenerCode.CLICK);
  }

  @Test
  public void givenListenerNotifier_whenSendOnClosed_ThenCallbackShouldBeCalled() {
    criteoResultReceiver.onReceiveResult(RESULT_CODE_SUCCESSFUL, getActionBundle(ACTION_CLOSED));

    verify(listenerNotifier).notifyFor(CriteoListenerCode.CLOSE);
  }

  private Bundle getActionBundle(int actionClosed) {
    Bundle bundle = new Bundle();
    bundle.putInt(INTERSTITIAL_ACTION, actionClosed);
    return bundle;
  }
}