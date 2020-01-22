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
public class BearcatPrivacyFunctionalTest {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Parameterized.Parameters
  public static Collection consents() {
    return Arrays.asList(new Object[][] {
        { "1YNN", "true", "EXPLICIT_YES", true },
        {"1YNY", "true", "EXPLICIT_YES", true},
        {"1---", "true", "EXPLICIT_YES", true},
        {"1ynn", "true", "EXPLICIT_YES", true},
        {"1yny", "true", "EXPLICIT_YES", true},
        { "1YNN", "true", "UNKNOWN", true },
        {"1YNY", "true", "UNKNOWN", true},
        {"1---", "true", "UNKNOWN", true},
        {"1ynn", "true", "UNKNOWN", true},
        {"1yny", "true", "UNKNOWN", true},
        {"", "", "", true},
        {null, null, null, true},
        {null, "", "", true},
        {"", "tr", "", true},
        {null, "tr","",true},

        { "1YNN", "true", "EXPLICIT_NO", false },
        {"1YNY", "true", "EXPLICIT_NO", false },
        {"1---", "true", "EXPLICIT_NO", false },
        {"1ynn", "true", "EXPLICIT_NO", false },
        {"1yny", "true", "EXPLICIT_NO", false },
        { "1YNN", "true", "EXPLICIT_NO", false },
        {"1YNY", "true", "EXPLICIT_NO", false},
        {"1---", "true", "EXPLICIT_NO", false},
        {"1ynn", "true", "EXPLICIT_NO", false},
        {"1yny", "true", "EXPLICIT_NO", false},
        {"", "", "EXPLICIT_NO", false},
        {null, "", "EXPLICIT_NO", false},
        {"", "tr", "EXPLICIT_NO", false},
        {null, "tr","EXPLICIT_NO", false},

        { "1YNN", "true", "POTENTIAL_WHITELIST", false },
        {"1YNY", "true", "POTENTIAL_WHITELIST", false },
        {"1---", "true", "POTENTIAL_WHITELIST", false },
        {"1ynn", "true", "POTENTIAL_WHITELIST", false },
        {"1yny", "true", "POTENTIAL_WHITELIST", false },
        { "1YNN", "true", "POTENTIAL_WHITELIST", false },
        {"1YNY", "true", "POTENTIAL_WHITELIST", false},
        {"1---", "true", "POTENTIAL_WHITELIST", false},
        {"1ynn", "true", "POTENTIAL_WHITELIST", false},
        {"1yny", "true", "POTENTIAL_WHITELIST", false},
        {"", "", "POTENTIAL_WHITELIST", false},
        {null, "", "POTENTIAL_WHITELIST", false},
        {"", "tr", "POTENTIAL_WHITELIST", false},
        {null, "tr","POTENTIAL_WHITELIST", false},

        {"1NNY", "", "EXPLICIT_YES", false},
        {"1nny", "", "EXPLICIT_YES", false},
        {"1nyn", "", "EXPLICIT_YES", false},
        {"", "true", "EXPLICIT_YES", false},
        {null, "true", "EXPLICIT_YES", false},

        {"1NNY", "", "UNKNOWN", false},
        {"1NYN", "", "UNKNOWN", false},
        {"1nny", "", "UNKNOWN", false},
        {"1nyn", "", "UNKNOWN", false},
        {"", "true", "UNKNOWN", false},
        {null, "true", "UNKNOWN", false},

        {"1NNY", "", "EXPLICIT_NO", false},
        {"1NYN", "", "EXPLICIT_NO", false},
        {"1nny", "", "EXPLICIT_NO", false},
        {"1nyn", "", "EXPLICIT_NO", false},
        {"", "true", "EXPLICIT_NO", false},
        {null, "true", "EXPLICIT_NO", false},

        {"1NNY", "", "EXPLICIT_NO",false},
        {"1NYN", "", "EXPLICIT_NO",false},
        {"1nny", "", "EXPLICIT_NO",false},
        {"1nyn", "", "EXPLICIT_NO",false},
        {"", "true", "EXPLICIT_NO",false},
        {null, "true", "EXPLICIT_NO", false},

        {"1NNY", "", "POTENTIAL_WHITELIST", false},
        {"1NYN", "", "POTENTIAL_WHITELIST", false},
        {"1nny", "", "POTENTIAL_WHITELIST", false},
        {"1nyn", "", "POTENTIAL_WHITELIST", false},
        {"", "true", "POTENTIAL_WHITELIST", false},
        {null, "true", "POTENTIAL_WHITELIST", false},

        {"1NNY", "", "DNT", false},
        {"1NYN", "", "DNT", false},
        {"1nny", "", "DNT", false},
        {"1nyn", "", "DNT", false},
        {"", "true", "DNT", false},
        {null, "true", "DNT", false}
    });
  }

  @Parameter(0)
  public String iabUsPrivacyString;

  @Parameter(1)
  public String binaryOptout;

  @Parameter(2)
  public String mopubConsentString;

  @Parameter(3)
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
    writeIntoDefaultSharedPrefs("MoPubConsent_String", mopubConsentString);

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
