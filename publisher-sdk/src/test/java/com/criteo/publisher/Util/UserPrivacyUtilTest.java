package com.criteo.publisher.Util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserPrivacyUtilTest {
  @Mock
  private SharedPreferences sharedPreferences;

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
}
