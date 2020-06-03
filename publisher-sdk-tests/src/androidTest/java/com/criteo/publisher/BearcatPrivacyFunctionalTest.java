package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.network.PubSdkApi;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class BearcatPrivacyFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Parameterized.Parameters
  public static Collection consents() {
    return Arrays.asList(new Object[][]{
        /** IAB: OK, Binary: OK, MOPUB: OK */
        {"1YNN", "false", "EXPLICIT_YES", true},
        {"1YNY", "false", "EXPLICIT_YES", true},
        {"1---", "false", "EXPLICIT_YES", true},
        {"1YN-", "false", "EXPLICIT_YES", true},
        {"1-N-", "false", "EXPLICIT_YES", true},
        {"", "false", "EXPLICIT_YES", true},

        {"1YNN", "false", "UNKNOWN", true},
        {"1YNY", "false", "UNKNOWN", true},
        {"1---", "false", "UNKNOWN", true},
        {"1YN-", "false", "UNKNOWN", true},
        {"1-N-", "false", "UNKNOWN", true},
        {"", "false", "UNKNOWN", true},

        {"", "", "", true},
        {null, null, null, true},
        {null, "", "", true},
        {"", "tr", "", true},
        {null, "tr", "", true},

        /** IAB: NOK, Binary: OK, MOPUB: OK */
        {"1NNY", "false", "EXPLICIT_YES", false},
        {"1NYN", "false", "EXPLICIT_YES", false},

        {"1NNY", "false", "UNKNOWN", false},
        {"1NYN", "false", "UNKNOWN", false},

        /** IAB: OK, Binary: NOK, Mopub: OK (IAB has precedence over binary) */
        {"1YNN", "true", "EXPLICIT_YES", true},
        {"1YNY", "true", "EXPLICIT_YES", true},
        {"1---", "true", "EXPLICIT_YES", true},
        {"1YN-", "true", "EXPLICIT_YES", true},
        {"1-N- ", "true", "EXPLICIT_YES", true},

        {"1YNN", "true", "UNKNOWN", true},
        {"1YNY", "true", "UNKNOWN", true},
        {"1---", "true", "UNKNOWN", true},
        {"1YN-", "true", "UNKNOWN", true},
        {"1-N- ", "true", "UNKNOWN", true},

        /** IAB: OK, Binary: OK, Mopub: NOK **/
        {"1YNN", "false", "POTENTIAL_WHITELIST", false},
        {"1YNY", "false", "POTENTIAL_WHITELIST", false},
        {"1---", "false", "POTENTIAL_WHITELIST", false},
        {"1YN-", "false", "POTENTIAL_WHITELIST", false},
        {"1-N- ", "false", "POTENTIAL_WHITELIST", false},

        {"1YNN", "false", "EXPLICIT_NO", false},
        {"1YNY", "false", "EXPLICIT_NO", false},
        {"1---", "false", "EXPLICIT_NO", false},
        {"1YN-", "false", "EXPLICIT_NO", false},
        {"1-N- ", "false", "EXPLICIT_NO", false},

        {"1YNN", "false", "DNT", false},
        {"1YNY", "false", "DNT", false},
        {"1---", "false", "DNT", false},
        {"1YN-", "false", "DNT", false},
        {"1-N- ", "false", "DNT", false},

        /** IAB: NOK, Binary: NOK, Mopub: OK */
        {"1NNY", "true", "EXPLICIT_YES", false},
        {"1NYN", "true", "EXPLICIT_YES", false},

        {"1NNY", "true", "UNKNOWN", false},
        {"1NYN", "true", "UNKNOWN", false},

        /** IAB: NOK, Binary: OK, Mopub: NOK */
        {"1NNY", "false", "EXPLICIT_NO", false},
        {"1NYN", "false", "EXPLICIT_NO", false},

        {"1NNY", "false", "DNT", false},
        {"1NNY", "false", "DNT", false},

        {"1NNY", "false", "POTENTIAL_WHITELIST", false},
        {"1NNY", "false", "POTENTIAL_WHITELIST", false},

        /** IAB: OK, Binary: NOK, Mopub: NOK */
        {"1YNN", "true", "EXPLICIT_NO", false},
        {"1YNY", "true", "EXPLICIT_NO", false},
        {"1---", "true", "EXPLICIT_NO", false},
        {"1YN-", "true", "EXPLICIT_NO", false},
        {"1-N- ", "true", "EXPLICIT_NO", false},

        {"1YNN", "true", "POTENTIAL_WHITELIST", false},
        {"1YNY", "true", "POTENTIAL_WHITELIST", false},
        {"1---", "true", "POTENTIAL_WHITELIST", false},
        {"1YN-", "true", "POTENTIAL_WHITELIST", false},
        {"1-N- ", "true", "POTENTIAL_WHITELIST", false},

        {"1YNN", "true", "DNT", false},
        {"1YNY", "true", "DNT", false},
        {"1---", "true", "DNT", false},
        {"1YN-", "true", "DNT", false},
        {"1-N- ", "true", "DNT", false},

        /** IAB: NOK, Binary: NOK, Mopub: NOK */
        {"1NNY", "true", "EXPLICIT_NO", false},
        {"1NYN", "true", "EXPLICIT_NO", false},

        {"1NNY", "true", "POTENTIAL_WHITELIST", false},
        {"1NYN", "true", "POTENTIAL_WHITELIST", false},

        {"1NNY", "true", "DNT", false},
        {"1NYN", "true", "DNT", false},
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

  @Inject
  private Context context;

  @Inject
  private AppEvents appEvents;

  @MockBean
  private PubSdkApi pubSdkApi;

  private SharedPreferences defaultSharedPreferences;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    mockedDependenciesRule.givenMockedRemoteConfigResponse(pubSdkApi);
  }

  @After
  public void after() {
    defaultSharedPreferences.edit().clear().apply();
  }

  @Test
  public void whenCriteoInit_GivenPrivacyStrings_VerifyIfBearcatShouldBeCalled() throws Exception {
    runTest(iabUsPrivacyString, mopubConsentString);
  }

  @Test
  public void whenCriteoInit_GivenPrivacyStringsInLowercase_VerifyIfBearcatShouldBeCalled()
      throws Exception {
    runTest(iabUsPrivacyString != null ? iabUsPrivacyString.toLowerCase(Locale.ROOT) : null,
        mopubConsentString != null ? mopubConsentString.toLowerCase(Locale.ROOT) : null);
  }

  private void runTest(
      String usPrivacyString,
      String mopubConsentString
  ) throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", usPrivacyString);
    writeIntoDefaultSharedPrefs("USPrivacy_Optout", binaryOptout);
    writeIntoDefaultSharedPrefs("MoPubConsent_String", mopubConsentString);

    givenInitializedCriteo(TestAdUnits.BANNER_320_50);

    appEvents.sendLaunchEvent();

    mockedDependenciesRule.waitForIdleState();

    if (callBearcat) {
      verify(pubSdkApi)
          .postAppEvent(any(Integer.class), any(String.class), any(String.class), any(String.class),
              any(Integer.class), any(), any());
    } else {
      verify(pubSdkApi, never())
          .postAppEvent(any(Integer.class), any(String.class), any(String.class), any(String.class),
              any(Integer.class), any(), any());
    }
  }

  private void writeIntoDefaultSharedPrefs(String key, String value) {
    Editor edit = defaultSharedPreferences.edit();
    edit.putString(key, value);
    edit.apply();
  }
}
