package com.criteo.pubsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.criteo.pubsdk.Util.HostAppUtil;
import com.google.gson.JsonObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GdprUnitTest {
    private Context context;
    private SharedPreferences.Editor editor;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
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

    @Test
    public void testGdprConsentWithCriteoAsApprovedVendor() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Criteo is at index 90 and set to 1
        String vendorConsent = "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001";
        editor.putString("IABConsent_SubjectToGDPR", subjectToGdpr);
        editor.putString("IABConsent_ConsentString", consentData);
        editor.putString("IABConsent_ParsedVendorConsents", vendorConsent);
        editor.apply();
        JsonObject gdprResponse = HostAppUtil.gdpr(context);
        Assert.assertEquals(gdprResponse.get("consentData").getAsString(), consentData);
        Assert.assertEquals(gdprResponse.get("gdprApplies").getAsBoolean(), true);
        Assert.assertEquals(gdprResponse.get("consentGiven").getAsBoolean(), true);
    }

    @Test
    public void testGdprConsentWithCriteoAsNotApprovedVendor() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Criteo is at index 90 and set to 0
        String vendorConsent = "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000000";
        editor.putString("IABConsent_SubjectToGDPR", subjectToGdpr);
        editor.putString("IABConsent_ConsentString", consentData);
        editor.putString("IABConsent_ParsedVendorConsents", vendorConsent);
        editor.apply();
        JsonObject gdprResponse = HostAppUtil.gdpr(context);
        Assert.assertEquals(gdprResponse.get("consentData").getAsString(), consentData);
        Assert.assertEquals(gdprResponse.get("gdprApplies").getAsBoolean(), true);
        Assert.assertEquals(gdprResponse.get("consentGiven").getAsBoolean(), false);
    }

    @Test
    public void testGDPRWithVendorConsentLessThan90CharsLong() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Vendor string is only 81 chars long
        String vendorConsent = "000000000000000000000000000000000000000000000000000000000000000000000000000000001";
        editor.putString("IABConsent_SubjectToGDPR", subjectToGdpr);
        editor.putString("IABConsent_ConsentString", consentData);
        editor.putString("IABConsent_ParsedVendorConsents", vendorConsent);
        editor.apply();
        JsonObject gdprResponse = HostAppUtil.gdpr(context);
        Assert.assertEquals(gdprResponse.get("consentData").getAsString(), consentData);
        Assert.assertEquals(gdprResponse.get("gdprApplies").getAsBoolean(), true);
        Assert.assertEquals(gdprResponse.get("consentGiven").getAsBoolean(), false);
    }

    @Test
    public void testGDPRWithVendorConsentOf90Chars() {
        String subjectToGdpr = "1";
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        //Vendor string is only 90 chars long
        String vendorConsent = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        editor.putString("IABConsent_SubjectToGDPR", subjectToGdpr);
        editor.putString("IABConsent_ConsentString", consentData);
        editor.putString("IABConsent_ParsedVendorConsents", vendorConsent);
        editor.apply();
        JsonObject gdprResponse = HostAppUtil.gdpr(context);
        Assert.assertEquals(gdprResponse.get("consentData").getAsString(), consentData);
        Assert.assertEquals(gdprResponse.get("gdprApplies").getAsBoolean(), true);
        Assert.assertEquals(gdprResponse.get("consentGiven").getAsBoolean(), false);
    }

    @Test
    public void testGdprConsentWithMissingProperties() {
        String consentData = "BOO9ZXlOO9auMAKABBITA1-AAAAZ17_______9______9uz_Gv_r_f__33e8_39v_h_7_u__7m_-zzV4-_lrQV1yPA1OrZArgEA";
        String vendorConsent = "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("IABConsent_ConsentString", consentData);
        editor.putString("IABConsent_ParsedVendorConsents", vendorConsent);
        editor.apply();
        JsonObject gdprResponse = HostAppUtil.gdpr(context);
        Assert.assertNull(gdprResponse);
    }
}
