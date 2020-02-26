package com.criteo.publisher.privacy.gdpr;

import android.support.annotation.NonNull;

public abstract class TcfGdprStrategy {
  @NonNull
  abstract String getConsentString();

  @NonNull
  abstract String getSubjectToGdpr();

  @NonNull
  abstract String getVendorConsents();

  @NonNull
  abstract Integer getVersion();

  boolean isProvided() {
    String subjectToGdpr = getSubjectToGdpr();
    String consentString = getConsentString();
    String vendorConsents = getVendorConsents();
    boolean isSubjectToGdprEmpty = subjectToGdpr.isEmpty();
    boolean isConsentStringEmpty = consentString.isEmpty();
    boolean isVendorConsentsEmpty = vendorConsents.isEmpty();

    return !isSubjectToGdprEmpty && !isConsentStringEmpty && !isVendorConsentsEmpty;
  }
}
