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

package com.criteo.publisher.privacy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.BuildConfigWrapper;
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
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);
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
