package com.criteo.publisher.privacy.gdpr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.Util.SafeSharedPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class Tcf1KeysSharedPreferencesSafetyTests {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private BuildConfigWrapper buildConfigWrapper;

  private Tcf1GdprStrategy tcf1GdprStrategy;
  private Tcf2GdprStrategy tcf2GdprStrategy;

  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context context = InstrumentationRegistry.getContext().getApplicationContext();
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(buildConfigWrapper.isDebug()).thenReturn(false);
    doReturn(buildConfigWrapper).when(dependencyProvider).provideBuildConfigWrapper();

    tcf1GdprStrategy = new Tcf1GdprStrategy(new SafeSharedPreferences(sharedPreferences));
    tcf2GdprStrategy = new Tcf2GdprStrategy(new SafeSharedPreferences(sharedPreferences));
  }

  @After
  public void tearDown() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove(Tcf1GdprStrategy.IAB_CONSENT_STRING_KEY);
    editor.remove(Tcf1GdprStrategy.IAB_SUBJECT_TO_GDPR_KEY);
    editor.remove(Tcf2GdprStrategy.IAB_TCString_Key);
    editor.remove(Tcf2GdprStrategy.IAB_GDPR_APPLIES_KEY);
    editor.apply();
  }

  @Test
  public void testRobustnessWhenAllKeysHaveBadType() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putInt(Tcf1GdprStrategy.IAB_CONSENT_STRING_KEY, 1);
    editor.putInt(Tcf1GdprStrategy.IAB_SUBJECT_TO_GDPR_KEY, 1);
    editor.putInt(Tcf2GdprStrategy.IAB_TCString_Key, 1);
    editor.putString(Tcf2GdprStrategy.IAB_GDPR_APPLIES_KEY, "");
    editor.apply();

    assertEquals("", tcf1GdprStrategy.getConsentString());
    assertEquals("", tcf1GdprStrategy.getSubjectToGdpr());
    assertEquals("", tcf2GdprStrategy.getConsentString());
    assertEquals("-1", tcf2GdprStrategy.getSubjectToGdpr());
  }
}
