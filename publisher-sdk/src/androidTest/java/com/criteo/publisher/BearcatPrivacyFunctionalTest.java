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

package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.SharedPreferences.Editor;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.SharedPreferencesFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(Parameterized.class)
public class BearcatPrivacyFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private static Collection<Object[]> consents(List<String> iabValues, List<String> binaryValues, boolean expected) {
    List<Object[]> results = new ArrayList<>();
    for (String iab : iabValues) {
      for (String binary : binaryValues) {
        results.add(new Object[]{ iab, binary, expected });
      }
    }
    return results;
  }

  @Parameterized.Parameters
  public static Collection consents() {
    List<String> iabOk = Arrays.asList("1YNN", "1YNY", "1---", "1YN-", "1-N-");
    List<String> iabNotOk = Arrays.asList("1NNY", "1NYN");
    List<String> iabInvalid = Arrays.asList("", null);

    List<String> binaryOk = Collections.singletonList("false");
    List<String> binaryNotOk = Collections.singletonList("true");
    List<String> binaryInvalid = Arrays.asList("", "tr", null);

    List<Object[]> results = new ArrayList<>();
    results.addAll(consents(iabOk, binaryOk, true));
    results.addAll(consents(iabInvalid, binaryInvalid, true));
    results.addAll(consents(iabInvalid, binaryOk, true));
    results.addAll(consents(iabOk, binaryInvalid, true));
    results.addAll(consents(iabOk, binaryNotOk, true)); // (IAB has precedence over binary)
    results.addAll(consents(iabNotOk, binaryNotOk, false));
    results.addAll(consents(iabNotOk, binaryOk, false));
    return results;
  }

  @Parameter(0)
  public String iabUsPrivacyString;

  @Parameter(1)
  public String binaryOptout;

  @Parameter(2)
  public boolean callBearcat;

  @Inject
  private AppEvents appEvents;

  @SpyBean
  private PubSdkApi pubSdkApi;

  @Inject
  private SharedPreferencesFactory sharedPreferencesFactory;

  @Test
  public void whenCriteoInit_GivenPrivacyStrings_VerifyIfBearcatShouldBeCalled() throws Exception {
    runTest(iabUsPrivacyString);
  }

  @Test
  public void whenCriteoInit_GivenPrivacyStringsInLowercase_VerifyIfBearcatShouldBeCalled()
      throws Exception {
    runTest(iabUsPrivacyString != null ? iabUsPrivacyString.toLowerCase(Locale.ROOT) : null);
  }

  private void runTest(
      String usPrivacyString
  ) throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", usPrivacyString);
    writeIntoDefaultSharedPrefs("USPrivacy_Optout", binaryOptout);

    givenInitializedCriteo();

    appEvents.sendLaunchEvent();

    mockedDependenciesRule.waitForIdleState();

    if (callBearcat) {
      verify(pubSdkApi)
          .postAppEvent(any(Integer.class), any(String.class), any(), any(String.class),
              any(Integer.class), any(), any()
          );
    } else {
      verify(pubSdkApi, never())
          .postAppEvent(any(Integer.class), any(String.class), any(String.class), any(String.class),
              any(Integer.class), any(), any());
    }
  }

  private void writeIntoDefaultSharedPrefs(String key, String value) {
    Editor edit = sharedPreferencesFactory.getApplication().edit();
    edit.putString(key, value);
    edit.apply();
  }
}
