package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static com.criteo.publisher.Util.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BearcatFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Mock
  private PubSdkApi api;

  private Context context;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    doReturn(api).when(mockedDependenciesRule.getDependencyProvider()).providePubSdkApi(any());
    context = InstrumentationRegistry.getContext().getApplicationContext();
  }

  @Test
  public void init_GivenUserAgentAndLaunchedActivity_SendInitEventWithUserAgent() throws Exception {
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    DeviceInfo deviceInfo = spy(dependencyProvider.provideDeviceInfo(context));
    doReturn(deviceInfo).when(dependencyProvider).provideDeviceInfo(any());
    doReturn(completedFuture("expectedUserAgent")).when(deviceInfo).getUserAgent();

    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());
    waitForIdleState();

    verify(api).postAppEvent(anyInt(), any(), any(), any(), anyInt(), eq("expectedUserAgent"));
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
