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

package com.criteo.publisher.model;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.criteo.publisher.mock.MockedDependenciesRule;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeviceInfoIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  private Executor executor;

  @Before
  public void setUp() throws Exception {
    executor = mockedDependenciesRule.getDependencyProvider()
        .provideThreadPoolExecutor();
  }

  @Test
  public void initialize_GivenPreviouslyFetchedUserAgent_RetrieveUserAgentOnce() throws Exception {
    DeviceInfo deviceInfo = spy(new DeviceInfo(context, executor));

    deviceInfo.getUserAgent().get();
    waitForIdleState();

    deviceInfo.initialize();
    waitForIdleState();

    verify(deviceInfo, times(1)).resolveUserAgent();
  }

  @Test
  public void getUserAgent_GivenInitializedDeviceInfo_ReturnsCompletedFuture() throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context, executor);

    deviceInfo.initialize();
    Future<String> userAgent = deviceInfo.getUserAgent();

    assertNotNull(userAgent.get());
  }

  @Test
  public void getUserAgent_GivenUninitializedDeviceInfoAndWaitForIdleState_ReturnsCompletedFuture()
      throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context, executor);

    Future<String> userAgent = deviceInfo.getUserAgent();
    waitForIdleState();

    assertNotNull(userAgent.get());
  }

  @Test
  public void getUserAgent_WhenOnMainThreadAndWaitForIdleState_RunAsyncAndReturnUncompletedFuture()
      throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context, executor);
    AtomicReference<Future<String>> userAgentAsyncRef = new AtomicReference<>();

    runOnMainThreadAndWait(() -> {
      userAgentAsyncRef.set(deviceInfo.getUserAgent());
    });

    assertNotNull(userAgentAsyncRef.get().get());
  }

  @Test
  public void getUserAgentFromWebViewAndWebSettings_WhenCompare_ShouldBeTheSame() {
    runOnMainThreadAndWait(() -> {
      WebView webView = new WebView(context);

      String webViewUa = webView.getSettings().getUserAgentString();
      String webSettingsUa = WebSettings.getDefaultUserAgent(context);

      assertEquals(webViewUa, webSettingsUa);
      webView.destroy();
    });
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}