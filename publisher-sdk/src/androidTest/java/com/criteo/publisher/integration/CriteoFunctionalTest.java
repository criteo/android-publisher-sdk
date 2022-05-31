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

package com.criteo.publisher.integration;

import static com.criteo.publisher.CriteoUtil.TEST_CP_ID;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.context.UserData;
import com.criteo.publisher.context.UserDataHolder;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.test.activity.DummyActivity;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CriteoFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule().withSpiedLogger();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(
      DummyActivity.class,
      false,
      false
  );

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Inject
  private Application application;

  @SpyBean
  private PubSdkApi api;

  @SpyBean
  private Config config;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @SpyBean
  private BidManager bidManager;

  @Inject
  private UserDataHolder userDataHolder;

  @SpyBean
  private Logger logger;

  @Test
  @SuppressWarnings("deprecation")
  public void setMoPubConsent_LogThatDeprecatedMethodIsCalled() throws Exception {
    givenInitializedCriteo().setMopubConsent("dummy");

    verify(logger).log(argThat(logMessage ->
        logMessage.getLevel() == Log.WARN && logMessage.getMessage().contains("Criteo#setMopubConsent")));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void moPubConsent_LogThatDeprecatedMethodIsCalled() throws Exception {
    new Criteo.Builder(application, "dummy").mopubConsent("dummy");

    verify(logger).log(argThat(logMessage ->
        logMessage.getLevel() == Log.WARN && logMessage.getMessage().contains("Criteo$Builder#mopubConsent")));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void adUnits_LogThatDeprecatedMethodIsCalled() throws Exception {
    new Criteo.Builder(application, "dummy").adUnits(new ArrayList<>());

    verify(logger).log(argThat(logMessage ->
        logMessage.getLevel() == Log.WARN && logMessage.getMessage().contains("Criteo$Builder#adUnits")));
  }

  @Test
  public void getVersion_GivenNotInitializedSdk_ReturnVersion() throws Exception {
    when(buildConfigWrapper.getSdkVersion()).thenReturn("1.2.3");

    String version = Criteo.getVersion();

    assertThat(version).isEqualTo("1.2.3");
  }

  @Test
  public void getVersion_GivenInitializedSdk_ReturnVersion() throws Exception {
    when(buildConfigWrapper.getSdkVersion()).thenReturn("1.2.3");

    givenInitializedCriteo();
    String version = Criteo.getVersion();

    assertThat(version).isEqualTo("1.2.3");
  }

  @Test
  public void getVersion_GivenExceptionWhileGettingVersion_DoNotThrowAndLog() throws Exception {
    RuntimeException exception = new RuntimeException();
    when(buildConfigWrapper.getSdkVersion()).thenThrow(exception);

    String version = Criteo.getVersion();

    assertThat(version).isEmpty();
    verify(logger).log(argThat(logMessage -> logMessage.getThrowable() == exception));
  }

  @Test
  public void init_GivenLaunchedActivity_CallConfigAndBearcat()
      throws Exception {
    givenInitializedCriteo();

    activityRule.launchActivity(new Intent());

    waitForBids();

    verify(api).loadConfig(any());
    verify(config).refreshConfig(any());

    verify(api).postAppEvent(anyInt(), any(), any(), eq("Launch"), anyInt(), any(), any());
    verify(api).postAppEvent(anyInt(), any(), any(), eq("Active"), anyInt(), any(), any());
  }

  @Test
  public void init_GivenCpIdAppIdAndVersion_CallConfigWithThose() throws Exception {
    when(buildConfigWrapper.getSdkVersion()).thenReturn("1.2.3");

    givenInitializedCriteo();
    waitForBids();

    verify(api).loadConfig(argThat(request -> {
      assertEquals(TEST_CP_ID, request.getCriteoPublisherId());
      assertEquals("com.criteo.publisher.test", request.getBundleId());
      assertEquals("1.2.3", request.getSdkVersion());

      return true;
    }));
  }

  @Test
  public void setUserData_GivenUserData_StoreItForLaterUse() throws Exception {
    UserData userData = mock(UserData.class);

    givenInitializedCriteo();

    Criteo.getInstance().setUserData(userData);

    assertThat(userDataHolder.get()).isSameAs(userData);
  }

  private void waitForBids() {
    mockedDependenciesRule.waitForIdleState();
  }

}
