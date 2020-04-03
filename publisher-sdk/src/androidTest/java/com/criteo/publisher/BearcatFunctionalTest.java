package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.util.CompletableFuture.completedFuture;
import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.privacy.gdpr.GdprData;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BearcatFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Mock
  private PubSdkApi api;
  private DependencyProvider dependencyProvider;

  private GdprData gdprData;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    doReturn(api).when(dependencyProvider).providePubSdkApi();

    UserPrivacyUtil userPrivacyUtil = dependencyProvider.provideUserPrivacyUtil();
    gdprData = userPrivacyUtil.getGdprData();
  }

  @Test
  public void init_GivenUserAgentAndLaunchedActivity_SendInitEventWithUserAgent() throws Exception {
    DeviceInfo deviceInfo = spy(dependencyProvider.provideDeviceInfo());
    doReturn(deviceInfo).when(dependencyProvider).provideDeviceInfo();
    doReturn(completedFuture("expectedUserAgent")).when(deviceInfo).getUserAgent();

    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());
    waitForIdleState();

    verify(api).postAppEvent(anyInt(), any(), any(), any(), anyInt(), eq("expectedUserAgent"),
        eq(gdprData));
  }

  @Test
  public void init_GivenInputAndLaunchedActivity_SendInitEventWithGivenData() throws Exception {
    AdvertisingInfo advertisingInfo = mock(AdvertisingInfo.class);
    when(advertisingInfo.isLimitAdTrackingEnabled(any())).thenReturn(false);
    when(advertisingInfo.getAdvertisingId(any())).thenReturn("myAdvertisingId");

    doReturn(advertisingInfo).when(dependencyProvider).provideAdvertisingInfo();

    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());
    waitForIdleState();

    verify(api).postAppEvent(
        eq(2379),
        eq("com.criteo.publisher.test"),
        eq("myAdvertisingId"),
        eq("Launch"),
        eq(0),
        any(),
        eq(gdprData));
  }

  @Test
  public void init_GivenLimitedAdTracking_SendInitEventWithDummyGaidAndLimitation()
      throws Exception {
    AdvertisingInfo advertisingInfo = mock(AdvertisingInfo.class);
    when(advertisingInfo.isLimitAdTrackingEnabled(any())).thenReturn(true);
    when(advertisingInfo.getAdvertisingId(any())).thenReturn("myAdvertisingId");

    doReturn(advertisingInfo).when(dependencyProvider).provideAdvertisingInfo();

    givenInitializedCriteo();
    activityRule.launchActivity(new Intent());
    waitForIdleState();

    verify(api).postAppEvent(
        anyInt(),
        any(),
        eq("00000000-0000-0000-0000-000000000000"),
        any(),
        eq(1),
        any(),
        eq(gdprData));
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
