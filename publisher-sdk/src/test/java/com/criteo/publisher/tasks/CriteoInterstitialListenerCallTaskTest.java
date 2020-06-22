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

package com.criteo.publisher.tasks;

import static com.criteo.publisher.CriteoErrorCode.ERROR_CODE_NO_FILL;
import static com.criteo.publisher.CriteoListenerCode.INVALID;
import static com.criteo.publisher.CriteoListenerCode.VALID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.CriteoListenerCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialListenerCallTaskTest {

  @Mock
  private CriteoInterstitialAdListener listener;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void run_GivenNullListener_DoesNotThrow() throws Exception {
    listener = null;

    CriteoInterstitialListenerCallTask task = createTask(INVALID);

    assertThatCode(task::run).doesNotThrowAnyException();
  }

  @Test
  public void run_GivenListenerThrowing_DoesNotThrow() throws Exception {
    doThrow(RuntimeException.class).when(listener).onAdReceived();

    CriteoInterstitialListenerCallTask task = createTask(VALID);

    assertThatCode(task::run).doesNotThrowAnyException();
  }

  @Test
  public void run_GivenInvalidCode_NotifyForFailure() throws InterruptedException {
    CriteoInterstitialListenerCallTask task = createTask(INVALID);
    task.run();

    verify(listener, times(0)).onAdReceived();
    verify(listener, times(1)).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  @Test
  public void run_GivenValidCode_NotifyForSuccess() throws InterruptedException {
    CriteoInterstitialListenerCallTask task = createTask(VALID);
    task.run();

    verify(listener, times(1)).onAdReceived();
    verify(listener, times(0)).onAdFailedToReceive(ERROR_CODE_NO_FILL);
  }

  private CriteoInterstitialListenerCallTask createTask(CriteoListenerCode code) {
    return new CriteoInterstitialListenerCallTask(listener, code);
  }

}