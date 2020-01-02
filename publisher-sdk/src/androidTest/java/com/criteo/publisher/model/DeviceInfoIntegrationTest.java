package com.criteo.publisher.model;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.ThreadingUtil;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.UserAgentCallback;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeviceInfoIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = InstrumentationRegistry.getContext().getApplicationContext();
  }

  @Test
  public void initialize_GivenCallback_CallItBack() throws Exception {
    UserAgentCallback callback = mock(UserAgentCallback.class);
    DeviceInfo deviceInfo = new DeviceInfo(context);

    deviceInfo.initialize(callback);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    verify(callback).done();
  }

  @Test
  public void initialize_GivenCallbackAndPreviouslyFetchedUserAgent_CallItBackAndRetrieveUserAgentOnce() throws Exception {
    UserAgentCallback callback = mock(UserAgentCallback.class);
    DeviceInfo deviceInfo = spy(new DeviceInfo(context));

    deviceInfo.getUserAgent().get();
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    deviceInfo.initialize(callback);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    verify(callback).done();
    verify(deviceInfo, times(1)).resolveUserAgent();
  }

  @Test
  public void getUserAgent_GivenInitializedDeviceInfo_ReturnsCompletedFuture() throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context);

    UserAgentCallback mock = mock(UserAgentCallback.class);
    deviceInfo.initialize(mock);
    Future<String> userAgent = deviceInfo.getUserAgent();

    assertNotNull(userAgent.get());
  }

  @Test
  public void getUserAgent_GivenUninitializedDeviceInfoAndWaitForIdleState_ReturnsCompletedFuture() throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context);

    Future<String> userAgent = deviceInfo.getUserAgent();
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    assertNotNull(userAgent.get());
  }

  @Test
  public void getUserAgent_WhenOnMainThreadAndWaitForIdleState_DoNotBlockAndReturnCompletedFuture() throws Exception {
    DeviceInfo deviceInfo = new DeviceInfo(context);

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