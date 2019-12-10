package com.criteo.publisher.Util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
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
    when(sharedPreferences.getString("IABUSPrivacy_String", "")).thenReturn("1YNNN");

    String iabUsPrivacyString = userPrivacyUtil.getIabUsPrivacyString();

    assertEquals("1YNNN", iabUsPrivacyString);
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
}
