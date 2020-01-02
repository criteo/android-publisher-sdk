package com.criteo.publisher.network;

import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.NottableString.not;

import android.content.Context;
import com.criteo.publisher.R;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;

public class PubSdkApiTest {

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  @SuppressWarnings("unused")
  private MockServerClient mockServerClient;

  @Mock
  private Context context;

  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(context.getString(R.string.event_url)).thenReturn("http://localhost:" + mockServerRule.getPort());

    api = new PubSdkApi(context);
  }

  @Test
  public void postAppEvent_GivenSenderId_SendGetRequest() throws Exception {
    api.postAppEvent(42, "", null, "", 0, "");

    mockServerClient.verify(request()
        .withPath("/appevent/v1/42")
        .withMethod("GET")
        .withHeader("Content-Type", "text/plain"), VerificationTimes.once());
  }

  @Test
  public void postAppEvent_GivenRequest_SendThemInQueryString() throws Exception {
    api.postAppEvent(42, "myApp", "myGaid", "myEvent", 1337, "");

    mockServerClient.verify(request()
        .withQueryStringParameter("appId", "myApp")
        .withQueryStringParameter("gaid", "myGaid")
        .withQueryStringParameter("eventType", "myEvent")
        .withQueryStringParameter("limitedAdTracking", "1337")
    );
  }

  @Test
  public void postAppEvent_GivenNoGaid_IsNotPutInQueryString() throws Exception {
    api.postAppEvent(42, "", null, "", 0, "");

    mockServerClient.verify(request()
        .withQueryStringParameter(not("gaid"))
    );
  }

  @Test
  public void postAppEvent_GivenUserAgent_SetItInHttpHeader() throws Exception {
    api.postAppEvent(42, "", null, "", 0, "myUserAgent");

    mockServerClient.verify(request()
        .withHeader("User-Agent", "myUserAgent")
    );
  }

}