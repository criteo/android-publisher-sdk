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

package com.criteo.publisher.privacy.gdpr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.SafeSharedPreferences;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Tcf1KeysSharedPreferencesSafetyTests {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @MockBean
  private BuildConfigWrapper buildConfigWrapper;

  @Inject
  private Context context;

  private Tcf1GdprStrategy tcf1GdprStrategy;
  private Tcf2GdprStrategy tcf2GdprStrategy;

  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);
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
    assertEquals("", tcf2GdprStrategy.getSubjectToGdpr());
  }
}
