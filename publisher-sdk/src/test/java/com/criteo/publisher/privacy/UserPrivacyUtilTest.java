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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.criteo.publisher.privacy.gdpr.GdprDataFetcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserPrivacyUtilTest {

  @Mock
  private SharedPreferences sharedPreferences;

  @Mock
  private GdprDataFetcher gdprDataFetcher;

  @Mock
  private Editor editor;

  private UserPrivacyUtil userPrivacyUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    userPrivacyUtil = new UserPrivacyUtil(sharedPreferences, gdprDataFetcher);
  }

  @Test
  public void testGetIabUsPrivacyString() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("fake_iab_usp");

    String iabUsPrivacyString = userPrivacyUtil.getIabUsPrivacyString();

    assertEquals("fake_iab_usp", iabUsPrivacyString);
  }

  @Test
  public void testIsIabConsentGiven_True_1YNN() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1YNN");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGivenOrNotApplicable();
    assertTrue(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_True_1YNY() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1YNY");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGivenOrNotApplicable();
    assertTrue(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_True_1dashdashdash() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1---");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGivenOrNotApplicable();
    assertTrue(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_False_1NNN() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1NNN");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGivenOrNotApplicable();
    assertFalse(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_True_2YNN() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("2YNN");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGivenOrNotApplicable();
    assertTrue(isConsent);
  }


  @Test
  public void testIsIabConsentGiven_False_Empty() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGivenOrNotApplicable();
    assertTrue(isConsent);
  }

  @Test
  public void testGetUsPrivacyOptout_WhenValueIsTrue() {
    when(sharedPreferences.getString("USPrivacy_Optout", "")).thenReturn("true");

    String uspOptout = userPrivacyUtil.getUsPrivacyOptout();

    assertEquals("true", uspOptout);
  }

  @Test
  public void testGetUsPrivacyOptout_WhenValueIsFalse() {
    when(sharedPreferences.getString("USPrivacy_Optout", "")).thenReturn("false");

    String uspOptout = userPrivacyUtil.getUsPrivacyOptout();

    assertEquals("false", uspOptout);
  }

  @Test
  public void testGetUsPrivacyOptout_WhenValueIsEmpty() {
    when(sharedPreferences.getString("USPrivacy_Optout", "")).thenReturn("");

    String uspOptout = userPrivacyUtil.getUsPrivacyOptout();

    assertEquals("", uspOptout);
  }

  @Test
  public void testSetUsPrivacyOptout_TrueValue() {
    when(sharedPreferences.edit()).thenReturn(editor);

    userPrivacyUtil.storeUsPrivacyOptout(true);

    verify(editor, timeout(1)).putString("USPrivacy_Optout", "true");
  }

  @Test
  public void testSetUsPrivacyOptout_FalseValue() {
    when(sharedPreferences.edit()).thenReturn(editor);

    userPrivacyUtil.storeUsPrivacyOptout(false);

    verify(editor, timeout(1)).putString("USPrivacy_Optout", "false");
  }

  @Test
  public void testSetMopubConsentValue() {
    when(sharedPreferences.edit()).thenReturn(editor);

    userPrivacyUtil.storeMopubConsent("fake_mopub_consent_value");

    verify(editor, timeout(1)).putString("MoPubConsent_String", "fake_mopub_consent_value");
  }

  @Test
  public void testGetMopubConsentValue() {
    when(sharedPreferences.getString("MoPubConsent_String", ""))
        .thenReturn("fake_mopub_consent_value");

    String mopubConsent = userPrivacyUtil.getMopubConsent();

    assertEquals("fake_mopub_consent_value", mopubConsent);
  }

  @Test
  public void testIsMopubConsentGiven_True() {
    assertMopubConsentGiven("EXPLICIT_YES", true);
    assertMopubConsentGiven("UNKNOWN", true);
    assertMopubConsentGiven("", true);
  }

  @Test
  public void testIsMopubConsentGiven_False() {
    assertMopubConsentGiven("EXPLICIT_NO", false);
    assertMopubConsentGiven("POTENTIAL_WHITELIST", false);
    assertMopubConsentGiven("DNT", false);
  }

  private void assertMopubConsentGiven(String mopubConsentString, boolean consentGiven) {
    when(sharedPreferences.getString("MoPubConsent_String", "")).thenReturn(mopubConsentString);
    assertEquals(consentGiven, userPrivacyUtil.isMopubConsentGivenOrNotApplicable());
  }

  @Test
  public void testIsCCPAConsentGiven_True() {
    assertCCPAConsentGiven("1YNN", "true", true);
    assertCCPAConsentGiven("1YNY", "true", true);
    assertCCPAConsentGiven("1---", "true", true);
    assertCCPAConsentGiven("1-N-", "true", true);
    assertCCPAConsentGiven("1YN-", "true", true);
    assertCCPAConsentGiven("1ynn", "true", true);
    assertCCPAConsentGiven("1yny", "true", true);
    assertCCPAConsentGiven("1-n-", "true", true);
    assertCCPAConsentGiven("1yn-", "true", true);
    assertCCPAConsentGiven("", "tr", true);
    assertCCPAConsentGiven("", "", true);
  }

  @Test
  public void testIsCCPAConsentGiven_False() {
    assertCCPAConsentGiven("", "true", false);
    assertCCPAConsentGiven("1NNY", "", false);
    assertCCPAConsentGiven("1NYN", "", false);
    assertCCPAConsentGiven("1nny", "", false);
    assertCCPAConsentGiven("1nyn", "", false);
    assertCCPAConsentGiven("", "true", false);
  }

  private void assertCCPAConsentGiven(
      String iabUsPrivacyString, String usPrivacyOptout,
      boolean consentGiven
  ) {
    givenUsPrivacySetup(iabUsPrivacyString, usPrivacyOptout);
    assertEquals(consentGiven, userPrivacyUtil.isCCPAConsentGivenOrNotApplicable());
  }

  private void givenUsPrivacySetup(String iabUsPrivacyString, String usPrivacyOptout) {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn(iabUsPrivacyString);
    when(sharedPreferences.getString("USPrivacy_Optout", "")).thenReturn(usPrivacyOptout);
  }
}
