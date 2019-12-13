package com.criteo.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
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
    BidManager bidManager = givenMockedBidManager();
    givenMockedDeviceInfo();
    adUnits = mock(List.class);

    createCriteo();

    verify(bidManager).prefetch(adUnits);
  }

  @Test
  public void new_GivenBidManagerAndNullAdUnits_ShouldCallPrefetchWithEmptyAdUnits() throws Exception {
    BidManager bidManager = givenMockedBidManager();
    givenMockedDeviceInfo();
    adUnits = null;

    createCriteo();

    verify(bidManager).prefetch(Collections.emptyList());
  }

  @Test
  public void new_GivenApplication_ShouldCreateSupportedScreenSizes() throws Exception {
    DeviceUtil deviceUtil = mock(DeviceUtil.class);

    Context context = application.getApplicationContext();
    when(dependencyProvider.provideDeviceUtil(context)).thenReturn(deviceUtil);

    createCriteo();

    verify(deviceUtil).createSupportedScreenSizes(application);
  }

  @Test
  public void new_GivenDependencyProvider_OnlyUseApplicationContextToBuildDependencies()
      throws Exception {
    // Used to verify transitive dependencies
    dependencyProvider = spy(DependencyProvider.getInstance());
    doReturn(mock(DeviceUtil.class)).when(dependencyProvider).provideDeviceUtil(any());

    Context applicationContext = mock(Context.class);
    when(application.getApplicationContext()).thenReturn(applicationContext);
    criteoPublisherId = "B-123456";

    createCriteo();

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(dependencyProvider, atLeastOnce()).provideDeviceUtil(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideAdUnitMapper(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideAndroidUtil(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideBidManager(contextCaptor.capture(), eq(criteoPublisherId));
    verify(dependencyProvider, atLeastOnce()).provideConfig(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideUserPrivacyUtil(contextCaptor.capture());

    assertThat(contextCaptor.getAllValues()).containsOnly(applicationContext);
  }

  private BidManager givenMockedBidManager() {
    BidManager bidManager = mock(BidManager.class);

    Context context = application.getApplicationContext();

    when(dependencyProvider.provideBidManager(context, criteoPublisherId)).thenReturn(bidManager);

    return bidManager;
  }

  private void givenMockedDeviceInfo() {
    Context context = application.getApplicationContext();
    DeviceInfo deviceInfo = dependencyProvider.provideDeviceInfo();
    doAnswer(answerVoid((Context ignoredContext, UserAgentCallback userAgentCallback) -> {
      userAgentCallback.done();
    })).when(deviceInfo).initialize(eq(context), any());
  }

  private CriteoInternal createCriteo() {
    return new CriteoInternal(application, adUnits, criteoPublisherId, dependencyProvider);
  }

}