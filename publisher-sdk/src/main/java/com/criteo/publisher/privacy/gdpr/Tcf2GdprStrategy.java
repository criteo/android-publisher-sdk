package com.criteo.publisher.privacy.gdpr;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/tree/master/TCFv2
 */
public class Tcf2GdprStrategy extends TcfGdprStrategy {

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
    return sharedPreferences.getString("IABTCF_gdprApplies", "");
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
}
