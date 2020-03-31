package com.criteo.publisher.privacy.gdpr;

import android.support.annotation.NonNull;

interface TcfGdprStrategy {
  @NonNull
  String getConsentString();

  @NonNull
  String getSubjectToGdpr();

  @NonNull
  Integer getVersion();

  boolean isProvided();
}
