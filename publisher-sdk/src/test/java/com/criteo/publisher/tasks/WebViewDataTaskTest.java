package com.criteo.publisher.tasks;

import static com.criteo.publisher.Util.CompletableFuture.completedFuture;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;

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
  private WebViewData webviewData;

  @Mock
  private DeviceInfo deviceInfo;

  @Mock
  private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

  private WebViewDataTask task;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    task = new WebViewDataTask(webviewData, deviceInfo, criteoInterstitialAdDisplayListener);
  }

  @Test
  public void backgroundJob_GivenUrlUserAgent_SetItInHttpRequest() throws Exception {
    when(deviceInfo.getUserAgent()).thenReturn(completedFuture("myUserAgent"));

    task.doInBackground("http://localhost:" + mockServerRule.getPort() + "/path");

    mockServerClient.verify(request()
        .withPath("/path")
        .withMethod("GET")
        .withHeader("Content-Type", "text/plain")
        .withHeader("User-Agent", "myUserAgent")
    );
  }

}