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

package com.criteo.publisher.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.criteo.publisher.csm.MetricRequest;
import com.criteo.publisher.logging.RemoteLogRecords;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.RemoteConfigRequest;
import com.criteo.publisher.privacy.gdpr.GdprData;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PubSdkApiTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockWebServer mockWebServer = new MockWebServer();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  private URL serverUrl;

  @Mock
  private GdprData gdprData;

  @SpyBean
  private JsonSerializer serializer;

  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    serverUrl = new URL("http://localhost:" + mockWebServer.getPort());

    when(buildConfigWrapper.getCdbUrl()).thenReturn(serverUrl.toString());
    when(buildConfigWrapper.getEventUrl()).thenReturn(serverUrl.toString());

    when(gdprData.consentData()).thenReturn("fake_consent_data");
    when(gdprData.gdprApplies()).thenReturn(false);
    when(gdprData.version()).thenReturn(1);

    api = new PubSdkApi(buildConfigWrapper, serializer);
  }

  @Test
  public void postLogs_GivenSerializedRequest_SendItWithPost() throws Exception {
    List<RemoteLogRecords> request = new ArrayList<>();
    String json = "{\"expectedJson\": 42}";

    givenSerializerWriting(request, json);

    mockWebServer.enqueue(new MockResponse().setResponseCode(204));

    api.postLogs(request);

    RecordedRequest webRequest = mockWebServer.takeRequest();
    assertThat(webRequest.getPath()).isEqualTo("/inapp/logs");
    assertThat(webRequest.getMethod()).isEqualTo("POST");
    assertThat(webRequest.getHeader("Content-Type")).isEqualTo("text/plain");
    assertThat(webRequest.getBody().snapshot().utf8()).isEqualTo(json);
  }

  @Test
  public void postLogs_GivenConnectionError_ThrowIOException() throws Exception {
    List<RemoteLogRecords> request = new ArrayList<>();

    givenConnectionError();

    assertThatCode(() -> api.postLogs(request)).isInstanceOf(IOException.class);
  }

  @Test
  public void postLogs_GivenHttpError_ThrowIOException() throws Exception {
    List<RemoteLogRecords> request = new ArrayList<>();

    mockWebServer.enqueue(new MockResponse().setResponseCode(400));

    assertThatCode(() -> api.postLogs(request)).isInstanceOf(IOException.class);
  }

  @Test
  public void postLogs_GivenLongRequestError_ThrowTimeoutError() throws Exception {
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenReturn(10);

    List<RemoteLogRecords> request = new ArrayList<>();

    mockWebServer.enqueue(new MockResponse()
        .throttleBody(1, 100, TimeUnit.MILLISECONDS)
        .setResponseCode(200));

    assertThatCode(() -> api.postLogs(request)).isInstanceOf(SocketTimeoutException.class);
  }

  @Test
  public void postCsm_GivenSerializedRequest_SendItWithPost() throws Exception {
    MetricRequest request = mock(MetricRequest.class);
    String json = "{\"expectedJson\": 42}";

    givenSerializerWriting(request, json);

    mockWebServer.enqueue(new MockResponse().setResponseCode(204));

    api.postCsm(request);

    RecordedRequest webRequest = mockWebServer.takeRequest();
    assertThat(webRequest.getPath()).isEqualTo("/csm");
    assertThat(webRequest.getMethod()).isEqualTo("POST");
    assertThat(webRequest.getHeader("Content-Type")).isEqualTo("text/plain");
    assertThat(webRequest.getBody().snapshot().utf8()).isEqualTo(json);
  }

  @Test
  public void postCsm_GivenConnectionError_ThrowIOException() throws Exception {
    MetricRequest request = mock(MetricRequest.class);

    givenConnectionError();

    assertThatCode(() -> api.postCsm(request)).isInstanceOf(IOException.class);
  }

  @Test
  public void postCsm_GivenHttpError_ThrowIOException() throws Exception {
    MetricRequest request = mock(MetricRequest.class);

    mockWebServer.enqueue(new MockResponse().setResponseCode(400));

    assertThatCode(() -> api.postCsm(request)).isInstanceOf(IOException.class);
  }

  @Test
  public void postCsm_GivenLongRequestError_ThrowTimeoutError() throws Exception {
    when(buildConfigWrapper.getNetworkTimeoutInMillis()).thenReturn(10);

    MetricRequest request = mock(MetricRequest.class);

    mockWebServer.enqueue(new MockResponse()
        .throttleBody(1, 100, TimeUnit.MILLISECONDS)
        .setResponseCode(200));

    assertThatCode(() -> api.postCsm(request)).isInstanceOf(SocketTimeoutException.class);
  }

  @Test
  public void postAppEvent_GivenSenderId_SendGetRequest() throws Exception {
    mockWebServer.enqueue(new MockResponse());

    api.postAppEvent(42, "", null, "", 0, "", "fake_consent_data");

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getRequestUrl().encodedPath()).isEqualTo("/appevent/v1/42");
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getHeader("Content-Type")).isEqualTo("text/plain");
  }

  @Test
  public void postAppEvent_GivenRequest_SendThemInQueryString() throws Exception {
    mockWebServer.enqueue(new MockResponse());

    api.postAppEvent(42, "myApp", "myGaid", "myEvent", 1337, "", "fake_consent_data");

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getRequestUrl().queryParameter("appId")).isEqualTo("myApp");
    assertThat(request.getRequestUrl().queryParameter("gaid")).isEqualTo("myGaid");
    assertThat(request.getRequestUrl().queryParameter("eventType")).isEqualTo("myEvent");
    assertThat(request.getRequestUrl().queryParameter("limitedAdTracking")).isEqualTo("1337");
    assertThat(request.getRequestUrl().queryParameter("gdpr_consent")).isEqualTo("fake_consent_data");
  }

  @Test
  public void postAppEvent_GivenNoGaid_IsNotPutInQueryString() throws Exception {
    mockWebServer.enqueue(new MockResponse());

    api.postAppEvent(42, "", null, "", 0, "", gdprData.consentData());

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getRequestUrl().queryParameter("gaid")).isNull();
  }

  @Test
  public void postAppEvent_GivenNoGdprData_IsNotPutInQueryString() throws Exception {
    mockWebServer.enqueue(new MockResponse());

    api.postAppEvent(42, "", "myGaid", "", 0, "", null);

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getRequestUrl().queryParameter("gdpr_consent")).isNull();
  }

  @Test
  public void postAppEvent_GivenUserAgent_SetItInHttpHeader() throws Exception {
    mockWebServer.enqueue(new MockResponse());

    api.postAppEvent(42, "", null, "", 0, "myUserAgent", "fake_consent_data");

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getHeader("User-Agent")).isEqualTo("myUserAgent");
  }

  @Test
  public void postAppEvent_GivenConnectionError_SetItInHttpHeader() throws Exception {
    givenConnectionError();

    assertThatCode(() -> api.postAppEvent(42, "", null, "", 0, "myUserAgent", "fake_consent_data"))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void postAppEvent_GivenHttpError_ThrowException() throws Exception {
    mockWebServer.enqueue(new MockResponse().setResponseCode(400));

    assertThatCode(() -> api.postAppEvent(42, "", null, "", 0, "myUserAgent", gdprData.consentData()))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void loadCdb_GivenCdbRequest_SendPostRequestWithJsonPayload() throws Exception {
    String json = "{\"payload\":\"my awesome payload\"}";
    CdbRequest cdbRequest = mock(CdbRequest.class);
    givenSerializerWriting(cdbRequest, json);

    mockWebServer.enqueue(new MockResponse().setResponseCode(204));

    api.loadCdb(cdbRequest, "");

    RecordedRequest webRequest = mockWebServer.takeRequest();
    assertThat(webRequest.getPath()).isEqualTo("/inapp/v2");
    assertThat(webRequest.getMethod()).isEqualTo("POST");
    assertThat(webRequest.getHeader("Content-Type")).isEqualTo("text/plain");
    assertThat(webRequest.getBody().snapshot().utf8()).isEqualTo(json);
  }

  @Test
  public void loadCdb_GivenBids_ReturnResponseWithBids() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    String json = "{\n"
        + "  \"slots\" : [\n"
        + "    {\n"
        + "      \"impId\": \"5def5cbbab53a9a7dea0639f43924c70\",\n"
        + "      \"placementId\": \"adunit_1\",\n"
        + "      \"arbitrageId\": \"0b391763-60db-4e4c-ac40-296eb083b3f3\","
        + "      \"cpm\" : \"1.00\",\n"
        + "      \"currency\" : \"EUR\",\n"
        + "      \"width\" : 100,\n"
        + "      \"height\" : 100,\n"
        + "      \"displayUrl\" : \"http://url.com\",\n"
        + "      \"ttl\" : 0\n"
        + "    }\n,"
        + "    {\n"
        + "      \"impId\": \"5def5cbbab53a9a7dea0639f43924c71\",\n"
        + "      \"placementId\": \"adunit_2\",\n"
        + "      \"arbitrageId\": \"0b391763-60db-4e4c-ac40-296eb083b3f4\","
        + "      \"cpm\" : \"2.00\",\n"
        + "      \"currency\" : \"USD\",\n"
        + "      \"width\" : 200,\n"
        + "      \"height\" : 300,\n"
        + "      \"displayUrl\" : \"https://url.fr\",\n"
        + "      \"ttl\" : 10\n"
        + "    }"
        + "  ]\n"
        + "}";

    mockWebServer.enqueue(new MockResponse().setBody(json));

    CdbResponse cdbResponse = api.loadCdb(cdbRequest, "");

    assertThat(cdbResponse).isNotNull();
    assertThat(cdbResponse.getSlots()).hasSize(2);
    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(0);

    assertThat(cdbResponse.getSlots().get(0).getPlacementId()).isEqualTo("adunit_1");
    assertThat(cdbResponse.getSlots().get(0).getCpmAsNumber()).isEqualTo(1.);
    assertThat(cdbResponse.getSlots().get(0).getCurrency()).isEqualTo("EUR");
    assertThat(cdbResponse.getSlots().get(0).getWidth()).isEqualTo(100);
    assertThat(cdbResponse.getSlots().get(0).getHeight()).isEqualTo(100);
    assertThat(cdbResponse.getSlots().get(0).getTtlInSeconds()).isEqualTo(0);
    assertThat(cdbResponse.getSlots().get(0).getDisplayUrl()).isEqualTo("http://url.com");

    assertThat(cdbResponse.getSlots().get(1).getPlacementId()).isEqualTo("adunit_2");
    assertThat(cdbResponse.getSlots().get(1).getCpmAsNumber()).isEqualTo(2.);
    assertThat(cdbResponse.getSlots().get(1).getCurrency()).isEqualTo("USD");
    assertThat(cdbResponse.getSlots().get(1).getWidth()).isEqualTo(200);
    assertThat(cdbResponse.getSlots().get(1).getHeight()).isEqualTo(300);
    assertThat(cdbResponse.getSlots().get(1).getTtlInSeconds()).isEqualTo(10);
    assertThat(cdbResponse.getSlots().get(1).getDisplayUrl()).isEqualTo("https://url.fr");
  }

  @Test
  public void loadCdb_GivenNoBidWithSilentMode_ReturnResponseWithSilence() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();
    String json = "{\"slots\":[],\"timeToNextCall\":300}";

    mockWebServer.enqueue(new MockResponse().setBody(json));

    CdbResponse cdbResponse = api.loadCdb(cdbRequest, "");

    assertThat(cdbResponse).isNotNull();
    assertThat(cdbResponse.getSlots()).isEmpty();
    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(300);
  }

  @Test
  public void loadCdb_GivenNoBid_ReturnEmptyResponse() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockWebServer.enqueue(new MockResponse().setResponseCode(204));

    CdbResponse cdbResponse = api.loadCdb(cdbRequest, "");

    assertThat(cdbResponse).isNotNull();
    assertThat(cdbResponse.getSlots()).isEmpty();
    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(0);
  }

  @Test
  public void loadCdb_GivenUserAgent_SetItInHttpHeader() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockWebServer.enqueue(new MockResponse().setResponseCode(204));

    api.loadCdb(cdbRequest, "myUserAgent");

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getHeader("User-Agent")).isEqualTo("myUserAgent");
  }

  @Test
  public void loadCdb_GivenConnectionError_ThrowException() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    givenConnectionError();

    assertThatCode(() -> api.loadCdb(cdbRequest, ""))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void loadCdb_GivenHttpError_ThrowException() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockWebServer.enqueue(new MockResponse().setResponseCode(400));

    assertThatCode(() -> api.loadCdb(cdbRequest, ""))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void loadConfig_GivenInput_SendGetRequestWithQueryParameters() throws Exception {
    RemoteConfigRequest request = RemoteConfigRequest.create(
        "myCpId",
        "myAppId",
        "myVersion",
        456,
        "myDeviceId"
    );

    String expectedJson = ""
        + "{\n"
        + "  \"cpId\" : \"myCpId\",\n"
        + "  \"bundleId\" : \"myAppId\",\n"
        + "  \"sdkVersion\" : \"myVersion\",\n"
        + "  \"rtbProfileId\": 456,\n"
        + "  \"deviceId\": \"myDeviceId\",\n"
        + "  \"deviceOs\": \"android\""
        + "}";

    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

    api.loadConfig(request);

    RecordedRequest webRequest = mockWebServer.takeRequest();
    assertThat(webRequest.getPath()).isEqualTo("/config/app");
    assertThat(webRequest.getMethod()).isEqualTo("POST");
    assertThat(webRequest.getBody().snapshot().utf8()).isEqualToIgnoringWhitespace(expectedJson);
  }

  @Test
  public void executeRawGet_GivenConnectionError_ThrowIt() throws Exception {
    givenConnectionError();

    assertThatCode(() -> api.executeRawGet(serverUrl))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void executeRawGet_GivenHttpError_ThrowIOException() throws Exception {
    mockWebServer.enqueue(new MockResponse().setResponseCode(400));

    assertThatCode(() -> api.executeRawGet(serverUrl))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void executeRawGet_GivenOkResponse_ReturnIt() throws Exception {
    mockWebServer.enqueue(new MockResponse().setBody("myResponse"));

    InputStream response = api.executeRawGet(serverUrl);

    assertThat(response).hasContent("myResponse");
  }

  @NonNull
  private CdbRequest givenEmptyCdbRequest() throws Exception {
    CdbRequest cdbRequest = mock(CdbRequest.class);
    givenSerializerWriting(cdbRequest, "{}");
    return cdbRequest;
  }

  private void givenSerializerWriting(Object expected, String json) throws IOException {
    doAnswer(answerVoid((Object ignored, OutputStream stream) -> {
      stream.write(json.getBytes(StandardCharsets.UTF_8));
    })).when(serializer).write(eq(expected), any());
  }

  private void givenConnectionError() throws IOException {
    mockWebServer.shutdown();
  }

}