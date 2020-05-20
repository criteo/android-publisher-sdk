package com.criteo.publisher.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.privacy.gdpr.GdprData;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GdprUnitTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  private SharedPreferences.Editor editor;

  @Inject
  private UserPrivacyUtil userPrivacyUtil;

  @Before
  public void setup() {
    editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
  }

  @After
  public void tearDown() {
    // Remove all properties to make sure every test case starts with its own properties
    editor.remove("IABConsent_SubjectToGDPR");
    editor.remove("IABConsent_ConsentString");
    editor.apply();
  }

  private void initializeGdprParameters(String subjectToGdpr, String consentData) {
    if (subjectToGdpr != null) {
      editor.putString("IABConsent_SubjectToGDPR", subjectToGdpr);
    }
    editor.putString("IABConsent_ConsentString", consentData);
    editor.apply();
  }

  @Test
  public void testGdprConsentWithCriteoAsApprovedVendor() {
    String subjectToGdpr = "1";
    String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";

    initializeGdprParameters(subjectToGdpr, consentData);
    GdprData gdprData = userPrivacyUtil.getGdprData();
    Assert.assertEquals(consentData, gdprData.consentData());
    Assert.assertEquals(true, gdprData.gdprApplies());
  }

  @Test
  public void testGdprConsentWithCriteoAsNotApprovedVendor() {
    String subjectToGdpr = "1";
    String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
    initializeGdprParameters(subjectToGdpr, consentData);

    GdprData gdprData = userPrivacyUtil.getGdprData();

    Assert.assertEquals(consentData, gdprData.consentData());
    Assert.assertEquals(true, gdprData.gdprApplies());
  }

  @Test
  public void testGDPRWithVendorConsentLessThan90CharsLong() {
    String subjectToGdpr = "1";
    String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
    initializeGdprParameters(subjectToGdpr, consentData);

    GdprData gdprData = userPrivacyUtil.getGdprData();

    Assert.assertEquals(consentData, gdprData.consentData());
    Assert.assertEquals(true, gdprData.gdprApplies());
  }

  @Test
  public void testGDPRWithVendorConsentOf90Chars() {
    String subjectToGdpr = "1";
    String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
    initializeGdprParameters(subjectToGdpr, consentData);

    GdprData gdprData = userPrivacyUtil.getGdprData();

    Assert.assertEquals(consentData, gdprData.consentData());
    Assert.assertEquals(true, gdprData.gdprApplies());
  }

  @Test
  public void testGdprConsentWithMissingProperties() {
    String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
    // SubjectToGdpr property is missing
    initializeGdprParameters(null, consentData);

    GdprData gdprData = userPrivacyUtil.getGdprData();

    Assert.assertNotNull(gdprData);
  }
}
