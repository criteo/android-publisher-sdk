package com.criteo.publisher.privacy.gdpr;

import android.support.annotation.NonNull;

public interface TcfGdprStrategy {
  @NonNull
  String getConsentString();

  @NonNull
  String getSubjectToGdpr();

  @NonNull
  String getVendorConsents();

  @NonNull
  Integer getVersion();

  boolean isProvided();
}
