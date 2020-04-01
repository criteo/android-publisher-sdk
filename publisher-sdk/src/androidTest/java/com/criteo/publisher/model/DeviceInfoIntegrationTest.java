package com.criteo.publisher.model;

import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.mock.MockedDependenciesRule;
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
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    deviceInfo.initialize();
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

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
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

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

}