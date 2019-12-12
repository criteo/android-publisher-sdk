package com.criteo.publisher;

import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInternalUnitTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Application application;

  private List<AdUnit> adUnits;

  private String criteoPublisherId = "B-000001";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DependencyProvider dependencyProvider;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    adUnits = new ArrayList<>();
  }

  @Test
  public void new_GivenBidManagerAndAdUnits_ShouldCallPrefetchWithAdUnits() throws Exception {
    BidManager bidManager = mock(BidManager.class);

    Context context = application.getApplicationContext();
    Config config = dependencyProvider.provideConfig(context);
    adUnits = mock(List.class);

    when(dependencyProvider.provideBidManager(
        eq(context), eq(criteoPublisherId), any(), eq(config),
        any(), any(), any(), any(), any(), any()))
        .thenReturn(bidManager);

    DeviceInfo deviceInfo = dependencyProvider.provideDeviceInfo();
    doAnswer(answerVoid((Context ignoredContext, UserAgentCallback userAgentCallback) -> {
      userAgentCallback.done();
    })).when(deviceInfo).initialize(eq(context), any());

    createCriteo();

    verify(bidManager).prefetch(adUnits);
  }

  @Test
  public void new_GivenDependencyProvider_BuildBidManagerWithProvidedDependencies() throws Exception {
    Context context = application.getApplicationContext();
    criteoPublisherId = "B-123456";

    createCriteo();

    verify(dependencyProvider).provideBidManager(
        context,
        criteoPublisherId,
        dependencyProvider.provideDeviceInfo(),
        dependencyProvider.provideConfig(context),
        dependencyProvider.provideDeviceUtil(context),
        dependencyProvider.provideAndroidUtil(context),
        dependencyProvider.provideLoggingUtil(),
        dependencyProvider.provideAdvertisingInfo(),
        dependencyProvider.provideClock(),
        dependencyProvider.provideUserPrivacyUtil(context)
    );
  }

  @Test
  public void new_GivenApplication_ShouldCreateSupportedScreenSizes() throws Exception {
    DeviceUtil deviceUtil = mock(DeviceUtil.class);

    Context context = application.getApplicationContext();
    when(dependencyProvider.provideDeviceUtil(context)).thenReturn(deviceUtil);

    createCriteo();

    verify(deviceUtil).createSupportedScreenSizes(application);
  }

  private CriteoInternal createCriteo() {
    return new CriteoInternal(application, adUnits, criteoPublisherId, dependencyProvider);
  }

}