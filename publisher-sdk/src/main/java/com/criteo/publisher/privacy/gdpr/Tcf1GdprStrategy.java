package com.criteo.publisher.privacy.gdpr;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Mobile%20In-App%20Consent%20APIs%20v1.0%20Final.md
 */
public class Tcf1GdprStrategy extends TcfGdprStrategy {

  private final SharedPreferences sharedPreferences;

  public Tcf1GdprStrategy(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  @Override
  @NonNull
  public String getConsentString() {
    return sharedPreferences.getString("IABConsent_ConsentString", "");
  }

  @Override
  @NonNull
  public String getSubjectToGdpr() {
    return sharedPreferences.getString("IABConsent_SubjectToGDPR", "");
  }

  @Override
  @NonNull
  public String getVendorConsents() {
    return sharedPreferences.getString("IABConsent_ParsedVendorConsents", "");
  }

  @Override
  @NonNull
  public Integer getVersion() {
    return 1;
  }

}
