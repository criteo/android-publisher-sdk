package com.criteo.publisher.privacy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserPrivacySharedPreferencesSafetyTests {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Mock
  private BuildConfigWrapper buildConfigWrapper;

  private UserPrivacyUtil userPrivacyUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(buildConfigWrapper.isDebug()).thenReturn(false);
    doReturn(buildConfigWrapper).when(dependencyProvider).provideBuildConfigWrapper();
    userPrivacyUtil = dependencyProvider.provideUserPrivacyUtil();
  }

  @After
  public void tearDown() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.remove(UserPrivacyUtil.IAB_USPRIVACY_SHARED_PREFS_KEY);
    editor.remove(UserPrivacyUtil.MOPUB_CONSENT_SHARED_PREFS_KEY);
    editor.remove(UserPrivacyUtil.OPTOUT_USPRIVACY_SHARED_PREFS_KEY);
    editor.apply();
  }

  @Test
  public void testRobustnessWhenAllKeysHaveBadType() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putInt(UserPrivacyUtil.IAB_USPRIVACY_SHARED_PREFS_KEY, 1);
    editor.putInt(UserPrivacyUtil.MOPUB_CONSENT_SHARED_PREFS_KEY, 1);
    editor.putInt(UserPrivacyUtil.OPTOUT_USPRIVACY_SHARED_PREFS_KEY, 1);
    editor.apply();

    assertEquals("", userPrivacyUtil.getIabUsPrivacyString());
    assertEquals("", userPrivacyUtil.getMopubConsent());
    assertEquals("", userPrivacyUtil.getUsPrivacyOptout());
  }
}
