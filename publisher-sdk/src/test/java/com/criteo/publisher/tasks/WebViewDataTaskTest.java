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

import static com.criteo.publisher.util.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpError.error;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

public class WebViewDataTaskTest {

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  @SuppressWarnings("unused")
  private MockServerClient mockServerClient;

  @Mock
  private WebViewData webViewData;

  @Mock
  private DeviceInfo deviceInfo;

  @Mock
  private CriteoInterstitialAdDisplayListener listener;

  private WebViewDataTask task;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(deviceInfo.getUserAgent()).thenReturn(completedFuture(""));

    task = new WebViewDataTask(webViewData, deviceInfo, listener);
  }

  @Test
  public void backgroundJob_GivenUrlAndUserAgent_SetItInHttpRequest() throws Exception {
    when(deviceInfo.getUserAgent()).thenReturn(completedFuture("myUserAgent"));

    task.doInBackground("http://localhost:" + mockServerRule.getPort() + "/path");

    mockServerClient.verify(request()
        .withPath("/path")
        .withMethod("GET")
        .withHeader("Content-Type", "text/plain")
        .withHeader("User-Agent", "myUserAgent")
    );
  }

  @Test
  public void backgroundJob_GivenServerRespondingContent_ReturnIt() throws Exception {
    mockServerClient.when(request()).respond(response()
        .withStatusCode(200)
        .withHeader("content-type", "text/javascript")
        .withBody("<script />"));

    String creative = task.doInBackground("http://localhost:" + mockServerRule.getPort() + "/path");

    assertThat(creative).isEqualTo("<script />");
  }

  @Test
  public void backgroundJob_GivenServerRespondingNoBody_ReturnEmpty() throws Exception {
    mockServerClient.when(request()).respond(response().withStatusCode(200));

    String creative = task.doInBackground("http://localhost:" + mockServerRule.getPort() + "/path");

    assertThat(creative).isEqualTo("");
  }

  @Test
  public void backgroundJob_GivenNotExistingUrl_ReturnNull() throws Exception {
    String creative = task.doInBackground("http://url.that.does.not.exist/path");

    assertThat(creative).isNull();
  }

  @Test
  public void backgroundJob_GivenIllFormedUrl_ReturnNull() throws Exception {
    String creative = task.doInBackground("not.a.url");

    assertThat(creative).isNull();
  }

  @Test
  public void backgroundJob_GivenConnectionError_ReturnNull() throws Exception {
    mockServerClient.when(request()).error(error().withDropConnection(true));

    String creative = task.doInBackground("http://localhost:" + mockServerRule.getPort() + "/path");

    assertThat(creative).isNull();
  }

  @Test
  public void backgroundJob_GivenHttpError_ReturnNull() throws Exception {
    mockServerClient.when(request()).respond(response().withStatusCode(400));

    String creative = task.doInBackground("http://localhost:" + mockServerRule.getPort() + "/path");

    assertThat(creative).isNull();
  }

  @Test
  public void onPostExecution_GivenNullCreative_NotifyForFailure() throws Exception {
    task.onPostExecute(null);

    verify(webViewData).downloadFailed();
    verify(listener).onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
  }

  @Test
  public void onPostExecution_GivenEmptyCreative_NotifyForFailure() throws Exception {
    task.onPostExecute("");

    verify(webViewData).downloadFailed();
    verify(listener).onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
  }

  @Test
  public void onPostExecution_GivenValidCreative_NotifyForSuccess() throws Exception {
    task.onPostExecute("content");

    verify(webViewData).downloadSucceeded();
    verify(webViewData).setContent("content");
    verify(listener).onAdReadyToDisplay();
  }

  @Test
  public void onPostExecution_GivenNoListenerAndInvalidCreative_DoesNotThrow() throws Exception {
    givenNoListener();

    assertThatCode(() -> {
      task.onPostExecute(null);
    }).doesNotThrowAnyException();
  }

  @Test
  public void onPostExecution_GivenNoListenerAndValidCreative_DoesNotThrow() throws Exception {
    givenNoListener();

    assertThatCode(() -> {
      task.onPostExecute("creative");
    }).doesNotThrowAnyException();
  }

  @Test
  public void onPostExecution_GivenThrowingListenerAndInvalidCreative_DoesNotThrow()
      throws Exception {
    givenThrowingListener();

    assertThatCode(() -> {
      task.onPostExecute(null);
    }).doesNotThrowAnyException();
  }

  @Test
  public void onPostExecution_GivenThrowingListenerAndValidCreative_DoesNotThrow()
      throws Exception {
    givenThrowingListener();

    assertThatCode(() -> {
      task.onPostExecute("creative");
    }).doesNotThrowAnyException();
  }

  private void givenNoListener() {
    task = new WebViewDataTask(webViewData, deviceInfo, null);
  }

  private void givenThrowingListener() {
    doThrow(RuntimeException.class).when(listener).onAdReadyToDisplay();
    doThrow(RuntimeException.class).when(listener).onAdFailedToDisplay(any());
  }

}