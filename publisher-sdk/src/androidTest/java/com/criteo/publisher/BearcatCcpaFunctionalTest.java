package com.criteo.publisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.test.activity.DummyActivity;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class BearcatCcpaFunctionalTest {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Parameterized.Parameters
  public static Collection consents() {
    return Arrays.asList(new Object[][] {
        { "1YNN", "true", true },
        {"1YNY", "true", true},
        {"1---", "true", true},
        {"1ynn", "true", true},
        {"1yny", "true", true},
        {"", "", true},
        {null, "", true},
        {"", "tr", true},
        {null, "tr", true},
        {"1NNY", "", false},
        {"1NYN", "", false},
        {"1nny", "", false},
        {"1nyn", "", false},
        {"", "true", false},
        {null, "true", false}
    });
  }

  @Parameter(0)
  public String iabUsPrivacyString;

  @Parameter(1)
  public String binaryOptout;

  @Parameter(2)
  public boolean callBearcat;

  @Mock
  private PubSdkApi pubSdkApi;

  private SharedPreferences defaultSharedPreferences;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
    defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());

    doReturn(pubSdkApi).when(dependencyProvider).providePubSdkApi(any());
  }

  @After
  public void after() {
    defaultSharedPreferences.edit().clear().commit();
  }

  @Test
  public void whenCriteoInit_GivenCCPAConsentIsGiven_VerifyBearcatIsCalled() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", iabUsPrivacyString);
    writeIntoDefaultSharedPrefs("USPrivacy_Optout", binaryOptout);

    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50);

    activityRule.launchActivity(new Intent());

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    if (callBearcat) {
      verify(pubSdkApi).postAppEvent(any(Integer.class), any(String.class), any(String.class), any(String.class), any(Integer.class), any());
    } else {
      verify(pubSdkApi, never()).postAppEvent(any(Integer.class), any(String.class), any(String.class), any(String.class), any(Integer.class), any());
    }
  }

  private void writeIntoDefaultSharedPrefs(String key, String value) {
    Editor edit = defaultSharedPreferences.edit();
    edit.putString(key, value);
    edit.commit();
  }
}
