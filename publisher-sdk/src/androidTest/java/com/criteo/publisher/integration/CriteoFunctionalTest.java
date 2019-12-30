package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.TEST_CP_ID;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.test.activity.DummyActivity;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CriteoFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private final BannerAdUnit validBannerAdUnit = TestAdUnits.BANNER_320_50;
  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;

  private Application application;

  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    application = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    api = spy(mockedDependenciesRule.getDependencyProvider().providePubSdkApi(application));
    doReturn(api).when(mockedDependenciesRule.getDependencyProvider()).providePubSdkApi(any());
  }

  @Test
  public void init_GivenPrefetchCachedBannerAndReInitWithSameBanner_CdbIsNotCallTheSecondTime() throws Exception {
    init_GivenPrefetchCachedAdAndReInitWithSameAdUnit_CdbIsNotCallTheSecondTime(validBannerAdUnit);
  }

  @Test
  public void init_GivenPrefetchCachedInterstitialAndReInitWithSameInterstitial_CdbIsNotCallTheSecondTime() throws Exception {
    init_GivenPrefetchCachedAdAndReInitWithSameAdUnit_CdbIsNotCallTheSecondTime(validInterstitialAdUnit);
  }

  private void init_GivenPrefetchCachedAdAndReInitWithSameAdUnit_CdbIsNotCallTheSecondTime(AdUnit adUnit)
      throws Exception {
    int dayTtl = 3600 * 24;

    doAnswer(invocation -> {
      Object response = invocation.callRealMethod();
      Cdb cdbResponse = (Cdb) response;
      cdbResponse.getSlots().forEach(slot -> {
        slot.setTtl(dayTtl);
      });
      return cdbResponse;
    }).when(api).loadCdb(any(), any());

    givenInitializedCriteo(adUnit);
    waitForBids();

    Criteo.init(application, TEST_CP_ID, Collections.singletonList(adUnit));
    waitForBids();

    verify(api, times(1)).loadCdb(any(), any());
  }

  @Test
  public void init_GivenPrefetchAdUnitAndLaunchedActivity_CallConfigAndCdbAndBearcat() throws Exception {
    givenInitializedCriteo(validBannerAdUnit);

    activityRule.launchActivity(new Intent());

    waitForBids();

    verify(api).loadCdb(any(), any());
    verify(api).loadConfig(any(), any(), any());
    verify(api).postAppEvent(anyInt(), any(), any(), any(), anyInt());
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

}
