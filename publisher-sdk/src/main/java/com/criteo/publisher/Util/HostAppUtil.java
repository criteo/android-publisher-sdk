package com.criteo.publisher.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;


public final class HostAppUtil {
    private static final String CONSENT_STRING = "IABConsent_ConsentString";
    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String VENDORS = "IABConsent_ParsedVendorConsents";
    private static final String CONSENT_DATA = "consentData";
    private static final String GDPR_APPLIES = "gdprApplies";
    private static final String CONSENT_GIVEN = "consentGiven";

    private HostAppUtil() {
    }

    public static JSONObject gdpr(Context context) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String consentString = mPreferences.getString(CONSENT_STRING, "");
        String subjectToGdpr = mPreferences.getString(SUBJECT_TO_GDPR, "");
        String vendorConsents = mPreferences.getString(VENDORS, "");
        JSONObject gdprConsent = null;
        if (!TextUtils.isEmpty(consentString) &&
                !TextUtils.isEmpty(subjectToGdpr) &&
                !TextUtils.isEmpty(vendorConsents)) {
            gdprConsent = new JSONObject();
            try {
                gdprConsent.put(CONSENT_DATA, consentString);
                gdprConsent.put(GDPR_APPLIES, "1".equals(subjectToGdpr));
                gdprConsent.put(CONSENT_GIVEN, (vendorConsents.length() > 90 && vendorConsents.charAt(90) == '1'));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return gdprConsent;
    }

}
