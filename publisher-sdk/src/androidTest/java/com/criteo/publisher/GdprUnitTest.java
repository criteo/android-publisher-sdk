package com.criteo.publisher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.criteo.publisher.Util.UserPrivacyUtil;

import com.criteo.publisher.Util.MockedDependenciesRule;
import org.json.JSONObject;
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

    private Context context;
    private SharedPreferences.Editor editor;
    private UserPrivacyUtil userPrivacyUtil;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        userPrivacyUtil = mockedDependenciesRule.getDependencyProvider().provideUserPrivacyUtil(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    }

    @After
    public void tearDown() {
        // Remove all properties to make sure every test case starts with its own properties
        editor.remove("IABConsent_SubjectToGDPR");
        editor.remove("IABConsent_ConsentString");
        editor.remove("IABConsent_ParsedVendorConsents");
        editor.apply();
    }

    private void initializeGdprParameters(String subjectToGdpr, String consentData, String vendorConsent) {
        if (subjectToGdpr != null) {
            editor.putString("IABConsent_SubjectToGDPR", subjectToGdpr);
        }
        editor.putString("IABConsent_ConsentString", consentData);
        editor.putString("IABConsent_ParsedVendorConsents", vendorConsent);
        editor.apply();
    }

    @Test
    public void testGdprConsentWithCriteoAsApprovedVendor() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Criteo is 91st character and set to 1
        String vendorConsent = "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001";
        initializeGdprParameters(subjectToGdpr, consentData, vendorConsent);
        JSONObject gdprResponse = userPrivacyUtil.gdpr();
        Assert.assertEquals(gdprResponse.optString("consentData"), consentData);
        Assert.assertEquals(gdprResponse.optBoolean("gdprApplies"), true);
        Assert.assertEquals(gdprResponse.optBoolean("consentGiven"), true);
    }

    @Test
    public void testGdprConsentWithCriteoAsNotApprovedVendor() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Criteo is 91st character and set to 0
        String vendorConsent = "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000000";
        initializeGdprParameters(subjectToGdpr, consentData, vendorConsent);
        JSONObject gdprResponse = userPrivacyUtil.gdpr();
        Assert.assertEquals(gdprResponse.optString("consentData"), consentData);
        Assert.assertEquals(gdprResponse.optBoolean("gdprApplies"), true);
        Assert.assertEquals(gdprResponse.optBoolean("consentGiven"), false);
    }

    @Test
    public void testGDPRWithVendorConsentLessThan90CharsLong() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Vendor string is only 81 chars long
        String vendorConsent = "000000000000000000000000000000000000000000000000000000000000000000000000000000001";
        initializeGdprParameters(subjectToGdpr, consentData, vendorConsent);
        JSONObject gdprResponse = userPrivacyUtil.gdpr();
        Assert.assertEquals(gdprResponse.optString("consentData"), consentData);
        Assert.assertEquals(gdprResponse.optBoolean("gdprApplies"), true);
        Assert.assertEquals(gdprResponse.optBoolean("consentGiven"), false);
    }

    @Test
    public void testGDPRWithVendorConsentOf90Chars() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Vendor string is only 90 chars long
        String vendorConsent = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        initializeGdprParameters(subjectToGdpr, consentData, vendorConsent);
        JSONObject gdprResponse = userPrivacyUtil.gdpr();
        Assert.assertEquals(gdprResponse.optString("consentData"), consentData);
        Assert.assertEquals(gdprResponse.optBoolean("gdprApplies"), true);
        Assert.assertEquals(gdprResponse.optBoolean("consentGiven"), false);
    }

    @Test
    public void testGdprConsentWithMissingProperties() {
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        String vendorConsent = "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001";
        //SubjectToGdpr property is missing
        initializeGdprParameters(null, consentData, vendorConsent);
        JSONObject gdprResponse = userPrivacyUtil.gdpr();
        Assert.assertNull(gdprResponse);
    }
}
