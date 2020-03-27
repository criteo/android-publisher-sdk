package com.criteo.publisher.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpError.error;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.verify.VerificationTimes.once;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.Util.JsonSerializer;
import com.criteo.publisher.csm.MetricRequest;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.RemoteConfigRequest;
import com.criteo.publisher.privacy.gdpr.GdprData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.MediaType;

public class PubSdkApiTest {

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  @SuppressWarnings("unused")
  private MockServerClient mockServerClient;

  @Mock
  private BuildConfigWrapper buildConfigWrapper;

  private URL serverUrl;

  @Mock
  private GdprData gdprData;

  @Mock
  private JsonSerializer serializer;

  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    serverUrl = new URL("http://localhost:" + mockServerRule.getPort());

    when(buildConfigWrapper.getCdbUrl()).thenReturn(serverUrl.toString());
    when(buildConfigWrapper.getEventUrl()).thenReturn(serverUrl.toString());
    when(buildConfigWrapper.getRemoteConfigUrl()).thenReturn(serverUrl.toString());

    when(gdprData.consentData()).thenReturn("fake_consent_data");
    when(gdprData.consentGiven()).thenReturn(false);
    when(gdprData.gdprApplies()).thenReturn(false);
    when(gdprData.version()).thenReturn(1);
    when(gdprData.toJSONObject()).thenCallRealMethod();

    api = new PubSdkApi(buildConfigWrapper, serializer);
  }

  @Test
  public void postCsm_GivenSerializedRequest_SendItWithPost() throws Exception {
    MetricRequest request = mock(MetricRequest.class);
    String json = "{\"expectedJson\": 42}";

    doAnswer(answerVoid((Object ignored, OutputStream stream) -> {
      stream.write(json.getBytes(StandardCharsets.UTF_8));
    })).when(serializer).write(eq(request), any());

    mockServerClient.when(request()).respond(response().withStatusCode(204));

    api.postCsm(request);

    mockServerClient.verify(request()
        .withPath("/csm")
        .withMethod("POST")
        .withContentType(MediaType.TEXT_PLAIN)
        .withBody(json, StandardCharsets.UTF_8), once());
  }

  @Test
  public void postCsm_GivenConnectionError_ReturnFailedFuture() throws Exception {
    MetricRequest request = mock(MetricRequest.class);

    mockServerClient.when(request()).error(error().withDropConnection(true));

    assertThatCode(() -> api.postCsm(request)).isInstanceOf(IOException.class);
  }

  @Test
  public void postCsm_GivenHttpError_ReturnFailedFuture() throws Exception {
    MetricRequest request = mock(MetricRequest.class);

    mockServerClient.when(request()).respond(response().withStatusCode(400));

    assertThatCode(() -> api.postCsm(request)).isInstanceOf(IOException.class);
  }

  @Test
  public void postAppEvent_GivenSenderId_SendGetRequest() throws Exception {
    api.postAppEvent(42, "", null, "", 0, "", gdprData);

    mockServerClient.verify(request()
        .withPath("/appevent/v1/42")
        .withMethod("GET")
        .withContentType(MediaType.TEXT_PLAIN), once());
  }

  @Test
  public void postAppEvent_GivenRequest_SendThemInQueryString() throws Exception {
    api.postAppEvent(42, "myApp", "myGaid", "myEvent", 1337, "", gdprData);

    mockServerClient.verify(request()
        .withQueryStringParameter("appId", "myApp")
        .withQueryStringParameter("gaid", "myGaid")
        .withQueryStringParameter("eventType", "myEvent")
        .withQueryStringParameter("limitedAdTracking", "1337")
        .withQueryStringParameter("gdprString", "eyJjb25zZW50RGF0YSI6ImZha2VfY29uc2VudF9kYXRhIiwiZ2RwckFwcGxpZXMiOmZhbHNlLCJjb25zZW50R2l2ZW4iOmZhbHNlLCJ2ZXJzaW9uIjoxfQ==")
    );
  }

  @Test
  public void postAppEvent_GivenNoGaid_IsNotPutInQueryString() throws Exception {
    api.postAppEvent(42, "", null, "", 0, "", gdprData);

    mockServerClient.verify(request()
        .withQueryStringParameter(not("gaid"))
    );
  }

  @Test
  public void postAppEvent_GivenNoGdprData_IsNotPutInQueryString() throws Exception {
    when(gdprData.consentData()).thenReturn("");

    api.postAppEvent(42, "", "myGaid", "", 0, "", gdprData);

    mockServerClient.verify(request()
        .withQueryStringParameter(not("gdprString"))
    );
  }

  @Test
  public void postAppEvent_GivenUserAgent_SetItInHttpHeader() throws Exception {
    api.postAppEvent(42, "", null, "", 0, "myUserAgent", gdprData);

    mockServerClient.verify(request()
        .withHeader("User-Agent", "myUserAgent")
    );
  }

  @Test
  public void loadCdb_GivenCdbRequest_SendPostRequestWithJsonPayload() throws Exception {
    String json = "{\"payload\":\"my awesome payload\"}";

    CdbRequest cdbRequest = mock(CdbRequest.class);
    when(cdbRequest.toJson()).thenReturn(new JSONObject(json));

    mockServerClient.when(request()).respond(response().withStatusCode(204));

    api.loadCdb(cdbRequest, "");

    mockServerClient.verify(request()
        .withPath("/inapp/v2")
        .withMethod("POST")
        .withContentType(MediaType.TEXT_PLAIN)
        .withBody(json, StandardCharsets.UTF_8), once()
    );
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

    mockServerClient.when(request()).respond(response(json));

    CdbResponse cdbResponse = api.loadCdb(cdbRequest, "");

    assertThat(cdbResponse).isNotNull();
    assertThat(cdbResponse.getSlots()).hasSize(2);
    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(0);

    assertThat(cdbResponse.getSlots().get(0).getPlacementId()).isEqualTo("adunit_1");
    assertThat(cdbResponse.getSlots().get(0).getCpmAsNumber()).isEqualTo(1.);
    assertThat(cdbResponse.getSlots().get(0).getCurrency()).isEqualTo("EUR");
    assertThat(cdbResponse.getSlots().get(0).getWidth()).isEqualTo(100);
    assertThat(cdbResponse.getSlots().get(0).getHeight()).isEqualTo(100);
    assertThat(cdbResponse.getSlots().get(0).getTtl()).isEqualTo(0);
    assertThat(cdbResponse.getSlots().get(0).getDisplayUrl()).isEqualTo("http://url.com");

    assertThat(cdbResponse.getSlots().get(1).getPlacementId()).isEqualTo("adunit_2");
    assertThat(cdbResponse.getSlots().get(1).getCpmAsNumber()).isEqualTo(2.);
    assertThat(cdbResponse.getSlots().get(1).getCurrency()).isEqualTo("USD");
    assertThat(cdbResponse.getSlots().get(1).getWidth()).isEqualTo(200);
    assertThat(cdbResponse.getSlots().get(1).getHeight()).isEqualTo(300);
    assertThat(cdbResponse.getSlots().get(1).getTtl()).isEqualTo(10);
    assertThat(cdbResponse.getSlots().get(1).getDisplayUrl()).isEqualTo("https://url.fr");
  }

  @Test
  public void loadCdb_GivenNoBidWithSilentMode_ReturnResponseWithSilence() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockServerClient.when(request()).respond(response("{\"slots\":[],\"timeToNextCall\":300}"));

    CdbResponse cdbResponse = api.loadCdb(cdbRequest, "");

    assertThat(cdbResponse).isNotNull();
    assertThat(cdbResponse.getSlots()).isEmpty();
    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(300);
  }

  @Test
  public void loadCdb_GivenNoBid_ReturnEmptyResponse() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockServerClient.when(request()).respond(response().withStatusCode(204));

    CdbResponse cdbResponse = api.loadCdb(cdbRequest, "");

    assertThat(cdbResponse).isNotNull();
    assertThat(cdbResponse.getSlots()).isEmpty();
    assertThat(cdbResponse.getTimeToNextCall()).isEqualTo(0);
  }

  @Test
  public void loadCdb_GivenUserAgent_SetItInHttpHeader() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockServerClient.when(request()).respond(response().withStatusCode(204));

    api.loadCdb(cdbRequest, "myUserAgent");

    mockServerClient.verify(request()
        .withHeader("User-Agent", "myUserAgent")
    );
  }

  @Test
  public void loadCdb_GivenConnectionError_ThrowException() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockServerClient.when(request()).error(error().withDropConnection(true));

    assertThatCode(() -> api.loadCdb(cdbRequest, ""))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void loadCdb_GivenHttpError_ThrowException() throws Exception {
    CdbRequest cdbRequest = givenEmptyCdbRequest();

    mockServerClient.when(request()).respond(response().withStatusCode(400));

    assertThatCode(() -> api.loadCdb(cdbRequest, ""))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void loadConfig_GivenInput_SendGetRequestWithQueryParameters() throws Exception {
    RemoteConfigRequest request = new RemoteConfigRequest(
        "myCpId",
        "myAppId",
        "myVersion");

    api.loadConfig(request);

    mockServerClient.verify(request()
        .withMethod("GET")
        .withPath("/v2.0/api/config")
        .withQueryStringParameter("cpId", "myCpId")
        .withQueryStringParameter("appId", "myAppId")
        .withQueryStringParameter("sdkVersion", "myVersion"), once());
  }

  @Test
  public void executeRawGet_GivenConnectionError_ThrowIt() throws Exception {
    mockServerClient.when(request()).error(error().withDropConnection(true));

    assertThatCode(() -> api.executeRawGet(serverUrl))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void executeRawGet_GivenHttpError_ThrowIOException() throws Exception {
    mockServerClient.when(request()).respond(response().withStatusCode(400));

    assertThatCode(() -> api.executeRawGet(serverUrl))
        .isInstanceOf(IOException.class);
  }

  @Test
  public void executeRawGet_GivenOkResponse_ReturnIt() throws Exception {
    mockServerClient.when(request()).respond(response("myResponse"));

    InputStream response = api.executeRawGet(serverUrl);

    assertThat(response).hasContent("myResponse");
  }

  @NonNull
  private CdbRequest givenEmptyCdbRequest() throws Exception {
    CdbRequest cdbRequest = mock(CdbRequest.class);
    when(cdbRequest.toJson()).thenReturn(new JSONObject());
    return cdbRequest;
  }

}