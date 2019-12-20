package com.criteo.publisher.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class UserPrivacyUtil {
    private static final String CONSENT_STRING = "IABConsent_ConsentString";
    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String VENDORS = "IABConsent_ParsedVendorConsents";
    private static final String CONSENT_DATA = "consentData";
    private static final String GDPR_APPLIES = "gdprApplies";
    private static final String CONSENT_GIVEN = "consentGiven";

    // Regex according to the CCPA IAB String format defined in
    // https://iabtechlab.com/wp-content/uploads/2019/11/U.S.-Privacy-String-v1.0-IAB-Tech-Lab.pdf
    private static final Pattern IAB_USPRIVACY_PATTERN = Pattern.compile("^1(Y|N|-|y|n){3}$");

    // List of IAB Strings representing a positive consent
    private static final List<String> IAB_USPRIVACY_WITH_CONSENT = Arrays.asList("1ynn", "1yny", "1---");

    // Key provided by the IAB CCPA Compliance Framework
    private static final String IAB_USPRIVACY = "IABUSPrivacy_String";

    // Storage key for the binary optout (for CCPA)
    private static final String OPTOUT_USPRIVACY = "USPrivacy_Optout";

    private final SharedPreferences sharedPreferences;

    public UserPrivacyUtil(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @VisibleForTesting
    UserPrivacyUtil(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public JSONObject gdpr() {
        String consentString = sharedPreferences.getString(CONSENT_STRING, "");
        String subjectToGdpr = sharedPreferences.getString(SUBJECT_TO_GDPR, "");
        String vendorConsents = sharedPreferences.getString(VENDORS, "");

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

    @NonNull
    public String getIabUsPrivacyString() {
        return sharedPreferences.getString(IAB_USPRIVACY, "");
    }

    public void storeUsPrivacyOptout(boolean uspOptout) {
        Editor edit = sharedPreferences.edit();
        edit.putString(OPTOUT_USPRIVACY, String.valueOf(uspOptout));
        edit.apply();
    }

    @NonNull
    public String getUsPrivacyOptout() {
        return sharedPreferences.getString(OPTOUT_USPRIVACY, "");
    }

    /**
     * Determine if CCPA consent is given.
     *
     * <p>
     *
     * <ul>
     *   <li>IAB has priority over the binary string</li>
     *   <li>If the IAB string is not well-formatted, we consider that the user has not opted-out</li>
     * </ul>
     *
     * More information can be found here: https://confluence.criteois.com/display/PP/CCPA+Buying+Policy
     *
     * @return {@code true} if consent is given, {@code false} otherwise
     */
    public boolean isCCPAConsentGiven() {
        String iabUsPrivacy = getIabUsPrivacyString();
        if (iabUsPrivacy.isEmpty()) {
            return isBinaryConsentGiven();
        }
        return isIABConsentGiven();
    }

    private boolean isBinaryConsentGiven() {
        String usPrivacyOptout = getUsPrivacyOptout();
        return Boolean.parseBoolean(usPrivacyOptout) != true;
    }

    private boolean isIABConsentGiven() {
        String iabUsPrivacy = getIabUsPrivacyString();

        return !IAB_USPRIVACY_PATTERN.matcher(iabUsPrivacy).matches() ||
                IAB_USPRIVACY_WITH_CONSENT.contains(iabUsPrivacy.toLowerCase());
    }
}
