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

import static com.criteo.publisher.CriteoUtil.clearCriteo;
import static com.criteo.publisher.CriteoUtil.getCriteoBuilder;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.SharedPreferences.Editor;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.SharedPreferencesFactory;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class UserPrivacyFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @SpyBean
  private PubSdkApi pubSdkApi;

  @Inject
  private SharedPreferencesFactory sharedPreferencesFactory;

  @Test
  public void whenCriteoInit_GivenUspIabNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", "fake_iab_usp");

    givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_iab_usp", cdb.getUser().uspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspIabEmpty_VerifyItIsNotPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", null);

    givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertNull(cdb.getUser().uspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.usPrivacyOptOut(true).init();

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("true", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.init();

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertNull(cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalseAfterFirstCall_VerifyFalseIsPassedToCdbOnTheSecondCall()
      throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();

    waitForIdleState();

    criteo.setUsPrivacyOptOut(false);
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480, mock(ContextData.class), mock(BidListener.class));

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.mopubConsent("fake_mopub_consent").init();

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentNotProvided_ThenProvidedAfterFirstCall_VerifyItIsPassedToCdbOnTheSecondCall()
      throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.init();

    waitForIdleState();

    criteo.setMopubConsent("fake_mopub_consent");
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480, mock(ContextData.class), mock(BidListener.class));

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentThroughSetter_ThenCriteoCleared_ThenVerifyItIsStillPassedToCdb()
      throws Exception {
    // given
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.init();

    waitForIdleState();

    criteo.setMopubConsent("fake_mopub_consent");

    // when
    clearCriteo();

    // then
    Criteo.Builder builder2 = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo2 = builder2.init();
    criteo2.getBidForAdUnit(TestAdUnits.BANNER_320_480, mock(ContextData.class), mock(BidListener.class));
    waitForIdleState();
    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(3)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  private void writeIntoDefaultSharedPrefs(String key, String value) {
    Editor edit = sharedPreferencesFactory.getApplication().edit();
    edit.putString(key, value);
    edit.commit();
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }
}
