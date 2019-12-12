package com.criteo.publisher.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.ThreadingUtil;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.Util.UserAgentCallback;
import org.junit.Rule;
import org.junit.Test;

public class DeviceInfoIntegrationTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  @Test
  public void initialize_GivenCallback_CallItBack() throws Exception {
    UserAgentCallback callback = mock(UserAgentCallback.class);
    DeviceInfo deviceInfo = new DeviceInfo();

    deviceInfo.initialize(InstrumentationRegistry.getContext(), callback);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    verify(callback).done();
  }

}