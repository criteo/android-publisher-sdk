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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.criteo.publisher.CriteoListenerCode;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.BuildConfigWrapper;
import javax.inject.Inject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WebViewDataTaskTest {

  @Rule
  public MockWebServer mockWebServer = new MockWebServer();

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private String displayUrl;

  @Mock
  private WebViewData webViewData;

  @Mock
  private DeviceInfo deviceInfo;

  @Mock
  private InterstitialListenerNotifier listenerNotifier;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @Inject
  private PubSdkApi api;

  private WebViewDataTask task;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    displayUrl = "http://localhost:" + mockWebServer.getPort() + "/path";
    when(deviceInfo.getUserAgent()).thenReturn(completedFuture(""));

    task = createTask();
  }

  @Test
  public void downloadCreative_GivenUrlAndUserAgent_SetItInHttpRequest() throws Exception {
    mockWebServer.enqueue(new MockResponse());
    when(deviceInfo.getUserAgent()).thenReturn(completedFuture("myUserAgent"));

    task.downloadCreative();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).isEqualTo("/path");
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getHeader("Content-Type")).isEqualTo("text/plain");
    assertThat(request.getHeader("User-Agent")).isEqualTo("myUserAgent");
  }

  @Test
  public void downloadCreative_GivenServerRespondingContent_ReturnIt() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("content-type", "text/javascript")
        .setBody("<script />"));

    String creative = task.downloadCreative();

    assertThat(creative).isEqualTo("<script />");
  }

  @Test
  public void run_GivenServerRespondingNoBody_ReturnEmpty() throws Exception {
    mockWebServer.enqueue(new MockResponse().setResponseCode(200));

    task.run();

    assertNotifyForFailure();
  }

  @Test
  public void run_GivenNotExistingUrl_NotifyForFailure() throws Exception {
    givenDisplayUrl("http://url.that.does.not.exist/path");

    task.run();

    assertNotifyForFailure();
  }

  @Test
  public void run_GivenIllFormedUrl_NotifyForFailure() throws Exception {
    givenDisplayUrl("not.a.url");

    task.run();

    assertNotifyForFailure();
  }

  @Test
  public void run_GivenConnectionError_NotifyForFailure() throws Exception {
    mockWebServer.shutdown();

    task.run();

    assertNotifyForFailure();
  }

  @Test
  public void run_GivenHttpError_ReturnNull() throws Exception {
    mockWebServer.enqueue(new MockResponse().setResponseCode(400));

    task.run();

    assertNotifyForFailure();
  }

  @Test
  public void run_GivenValidCreative_NotifyForSuccess() throws Exception {
    givenDownloadedCreative("content");

    task.run();

    assertNotifyForSuccess();
  }

  private void givenDisplayUrl(@NonNull String displayUrl) {
    this.displayUrl = displayUrl;
    task = createTask();
  }

  private void givenDownloadedCreative(@NonNull String creative) {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody(creative));
  }

  private void assertNotifyForFailure() {
    verify(webViewData).downloadFailed();
    verify(listenerNotifier).notifyFor(CriteoListenerCode.INVALID_CREATIVE);
  }

  private void assertNotifyForSuccess() {
    verify(webViewData).downloadSucceeded();
    verify(webViewData).setContent("content");
    verify(listenerNotifier).notifyFor(CriteoListenerCode.VALID);
  }

  private WebViewDataTask createTask() {
    return new WebViewDataTask(
        displayUrl,
        webViewData,
        deviceInfo,
        listenerNotifier,
        api
    );
  }

}