package com.criteo.pubsdk.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.criteo.pubsdk.R;
import com.google.gson.JsonObject;


public final class HostAppUtil {
    private static final String CONSENT_STRING = "IABConsent_ConsentString";
    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String VENDORS = "IABConsent_ParsedVendorConsents";
    private static final String CONSENT_DATA = "consentData";
    private static final String GDPR_APPLIES = "gdprApplies";
    private static final String CONSENT_GIVEN = "consentGiven";

    private HostAppUtil() {
    }

    public static String getPublisherId(Context context) {
        return context.getString(R.string.criteo_publisher_id);
    }

    public static JsonObject gdpr(Context context) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String consentString = mPreferences.getString(CONSENT_STRING, "");
        String subjectToGdpr = mPreferences.getString(SUBJECT_TO_GDPR, "");
        String vendorConsents = mPreferences.getString(VENDORS, "");
        JsonObject gdprConsent = null;
        if (!TextUtils.isEmpty(consentString) &&
                !TextUtils.isEmpty(subjectToGdpr) &&
                !TextUtils.isEmpty(vendorConsents)) {
            gdprConsent = new JsonObject();
            gdprConsent.addProperty(CONSENT_DATA, consentString);
            gdprConsent.addProperty(GDPR_APPLIES, "1".equals(subjectToGdpr));
            gdprConsent.addProperty(CONSENT_GIVEN, vendorConsents.charAt(90) == '1');
        }
        return gdprConsent;
    }

}
