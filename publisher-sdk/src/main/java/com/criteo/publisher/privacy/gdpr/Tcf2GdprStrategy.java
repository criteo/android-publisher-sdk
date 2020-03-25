package com.criteo.publisher.privacy.gdpr;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/tree/master/TCFv2
 */
public class Tcf2GdprStrategy implements TcfGdprStrategy {

  private static final int GDPR_APPLIES_UNSET = -1;

  private final SharedPreferences sharedPreferences;

  public Tcf2GdprStrategy(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  @Override
  @NonNull
  public String getConsentString() {
    return sharedPreferences.getString("IABTCF_TCString", "");
  }

  @Override
  @NonNull
  public String getSubjectToGdpr() {
    int gdprApplies = sharedPreferences.getInt("IABTCF_gdprApplies", GDPR_APPLIES_UNSET);
    return String.valueOf(gdprApplies);
  }

  @Override
  @NonNull
  public String getVendorConsents() {
    return sharedPreferences.getString("IABTCF_VendorConsents", "");
  }

  @Override
  @NonNull
  public Integer getVersion() {
    return 2;
  }

  @Override
  public boolean isProvided() {
    String subjectToGdpr = getSubjectToGdpr();
    String consentString = getConsentString();
    String vendorConsents = getVendorConsents();
    boolean isSubjectToGdprEmpty = Integer.valueOf(subjectToGdpr).equals(GDPR_APPLIES_UNSET);
    boolean isConsentStringEmpty = consentString.isEmpty();
    boolean isVendorConsentsEmpty = vendorConsents.isEmpty();

    return !isSubjectToGdprEmpty && !isConsentStringEmpty && !isVendorConsentsEmpty;
  }
}
