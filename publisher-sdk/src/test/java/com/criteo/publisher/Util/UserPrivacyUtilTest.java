package com.criteo.publisher.Util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserPrivacyUtilTest {
  @Mock
  private SharedPreferences sharedPreferences;

  @Mock
  private Editor editor;

  private UserPrivacyUtil userPrivacyUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    userPrivacyUtil = new UserPrivacyUtil(sharedPreferences);
  }

  @Test
  public void testGetIabUsPrivacyString() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("fake_iab_usp");

    String iabUsPrivacyString = userPrivacyUtil.getIabUsPrivacyString();

    assertEquals("fake_iab_usp", iabUsPrivacyString);
  }

  @Test
  public void testIsConsentGiven_True() {
    assertConsentGiven("1YNN", "true", true);
    assertConsentGiven("1YNY", "true", true);
    assertConsentGiven("1---", "true", true);
    assertConsentGiven("1ynn", "true", true);
    assertConsentGiven("1yny", "true", true);
    assertConsentGiven("", "tr", true);
    assertConsentGiven("", "", true);
  }

  @Test
  public void testIsConsentGiven_False() {
    assertConsentGiven("", "true", false);
    assertConsentGiven("1NNY", "", false);
    assertConsentGiven("1NYN", "", false);
    assertConsentGiven("1nny", "", false);
    assertConsentGiven("1nyn", "", false);
    assertConsentGiven("", "true", false);
  }

  @Test
  public void testIsIabConsentGiven_True_1YNN() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1YNN");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGiven();
    assertTrue(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_True_1YNY() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1YNY");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGiven();
    assertTrue(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_True_1dashdashdash() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1---");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGiven();
    assertTrue(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_False_1NNN() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1NNN");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGiven();
    assertFalse(isConsent);
  }

  @Test
  public void testIsIabConsentGiven_True_2YNN() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("2YNN");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGiven();
    assertTrue(isConsent);
  }


  @Test
  public void testIsIabConsentGiven_False_Empty() {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("");
    boolean isConsent = userPrivacyUtil.isCCPAConsentGiven();
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
    when(sharedPreferences.getString("MoPubConsent_String", "")).thenReturn("fake_mopub_consent_value");

    String mopubConsent = userPrivacyUtil.getMopubConsent();

    assertEquals("fake_mopub_consent_value", mopubConsent);
  }

  private void assertConsentGiven(String iabUsPrivacyString, String usPrivacyOptout, boolean consentGiven) {
    givenUsPrivacySetup(iabUsPrivacyString, usPrivacyOptout);
    assertEquals(consentGiven, userPrivacyUtil.isCCPAConsentGiven());
  }

  private void givenUsPrivacySetup(String iabUsPrivacyString, String usPrivacyOptout) {
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn(iabUsPrivacyString);
    when(sharedPreferences.getString("USPrivacy_Optout", "")).thenReturn(usPrivacyOptout);
  }
}
