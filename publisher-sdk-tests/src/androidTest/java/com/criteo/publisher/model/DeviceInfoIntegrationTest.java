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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.UserAgentCallback;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeviceInfoIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  private Executor runOnUiThreadExecutor;

  @Before
  public void setUp() throws Exception {
    runOnUiThreadExecutor = mockedDependenciesRule.getDependencyProvider()
        .provideRunOnUiThreadExecutor();
  }

  @Test
  public void initialize_GivenPreviouslyFetchedUserAgent_RetrieveUserAgentOnce() throws Exception {
    DeviceInfo deviceInfo = spy(new DeviceInfo(context, runOnUiThreadExecutor));

    deviceInfo.getUserAgent().get();
    waitForIdleState();

    deviceInfo.initialize();
    waitForIdleState();

    verify(deviceInfo, times(1)).resolveUserAgent();
  }

  @Test
  public void getUserAgent_GivenInitializedDeviceInfo_ReturnsCompletedFuture() throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context, runOnUiThreadExecutor);

    UserAgentCallback mock = mock(UserAgentCallback.class);
    deviceInfo.initialize();
    Future<String> userAgent = deviceInfo.getUserAgent();

    assertNotNull(userAgent.get());
  }

  @Test
  public void getUserAgent_GivenUninitializedDeviceInfoAndWaitForIdleState_ReturnsCompletedFuture()
      throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context, runOnUiThreadExecutor);

    Future<String> userAgent = deviceInfo.getUserAgent();
    waitForIdleState();

    assertNotNull(userAgent.get());
  }

  @Test
  public void getUserAgent_WhenOnMainThreadAndWaitForIdleState_DoNotBlockAndReturnCompletedFuture()
      throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context, runOnUiThreadExecutor);

    new Handler(Looper.getMainLooper()).post(() -> {
      String userAgent;
      try {
        userAgent = deviceInfo.getUserAgent().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }

      assertNotNull(userAgent);
    });
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

}