package com.criteo.pubsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class GdprUnitTest {
    private Context context ;
    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }
    @Test
    public void vendorTest(){
        String consentDatagiven="1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("IABConsent_ParsedVendorConsents", consentDatagiven);
        editor.apply();
        SharedPreferences preference=PreferenceManager.getDefaultSharedPreferences(context);
        String vendor=preference.getString("IABConsent_ParsedVendorConsents", null);
        String a_letter = Character.toString(vendor.charAt(90));
        assertEquals(Integer.parseInt(a_letter), 1);
    }
    @Test
    public void subjectToGdpr(){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("IABConsent_SubjectToGDPR", "1");
        editor.apply();
        SharedPreferences preference=PreferenceManager.getDefaultSharedPreferences(context);
        String isGdpr=preference.getString("IABConsent_SubjectToGDPR", "0");
        assertEquals(Integer.parseInt(isGdpr), 1);
    }
    @Test
    public void consentDataTest(){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("IABConsent_ConsentString", "1");
        editor.apply();
        SharedPreferences preference=PreferenceManager.getDefaultSharedPreferences(context);
        String consentData=preference.getString("IABConsent_ConsentString", null);
        assertNotNull(consentData);
    }
}
