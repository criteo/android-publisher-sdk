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

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.SharedPreferencesFactory;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GdprTcfEndToEndWithCdbTests {

  private static final String TCF2_CONSENT_NOT_GIVEN = "COwJDpQOwJDpQIAAAAENAPCgAAAAAAAAAAAAAxQAgAsABiAAAAAA";
  private static final String TCF2_CONSENT_GIVEN = "COwJDpQOwJDpQIAAAAENAPCgAAAAAAAAAAAAAxQAQAtgAAAA";
  private static final String TCF1_CONSENT_NOT_GIVEN = "BOnz82JOnz82JABABBFRCPgAAAAFuABABAA";
  private static final String TCF1_CONSENT_GIVEN = "BOnz814Onz814ABABBFRCP4AAAAFuABAC2A";

  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @SpyBean
  private PubSdkApi api;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @Inject
  private SharedPreferencesFactory sharedPreferencesFactory;

  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() {
    sharedPreferences = sharedPreferencesFactory.getApplication();
  }

  @After
  public void tearDown() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove("IABTCF_TCString");
    editor.remove("IABTCF_gdprApplies");
    editor.remove("IABConsent_ConsentString");
    editor.remove("IABConsent_SubjectToGDPR");
    editor.apply();
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentNotGiven_ShouldNotBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2NegativeConsent();

    // when
    whenBidding();

    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentGiven_ShouldBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf2WithIllFormedConsent_ShouldBid() throws Exception {
    // given
    givenInitializedSdk();
    givenTcf2IllFormedConsent();
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);

    // when
    whenBidding();

    // then
    verify(api).loadCdb(any(), any());
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentGiven_ShouldBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentNotGiven_ShouldNotBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1NegativeConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentGiven_andTcf1ConsentNotGiven_ShouldBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2PositiveConsent();
    givenTcf1NegativeConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentGiven_andTcf2ConsentGiven_ShouldBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1PositiveConsent();
    givenTcf2PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentNotGiven_andTcf2ConsentNotGiven_ShouldBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1NegativeConsent();
    givenTcf2NegativeConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentNotGiven_andTcf1ConsentGiven_ShouldNotBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2NegativeConsent();
    givenTcf1PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  private void givenInitializedSdk() throws Exception {
    givenInitializedCriteo();
    waitForBids();
  }

  private void waitForBids() {
    mockedDependenciesRule.waitForIdleState();
  }

  private void givenTcf1PositiveConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABConsent_ConsentString", TCF1_CONSENT_GIVEN);
    editor.putString("IABConsent_SubjectToGDPR", "1");
    editor.apply();
  }

  private void givenTcf1NegativeConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABConsent_ConsentString", TCF1_CONSENT_NOT_GIVEN);
    editor.putString("IABConsent_SubjectToGDPR", "1");
    editor.apply();
  }

  private void givenTcf2PositiveConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABTCF_TCString", TCF2_CONSENT_GIVEN);
    editor.putInt("IABTCF_gdprApplies", 1);
    editor.apply();
  }

  private void givenTcf2NegativeConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABTCF_TCString", TCF2_CONSENT_NOT_GIVEN);
    editor.putInt("IABTCF_gdprApplies", 1);
    editor.apply();
  }

  private void givenTcf2IllFormedConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABTCF_TCString", "wrong");
    editor.putString("IABTCF_gdprApplies", "bad");
    editor.apply();
  }

  private void whenBidding() {
    Criteo.getInstance().loadBid(validInterstitialAdUnit, new ContextData(), ignore -> { /* no op */ });
    waitForBids();
  }
}
