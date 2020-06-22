/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.privacy.gdpr;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.util.SafeSharedPreferences;

/**
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Mobile%20In-App%20Consent%20APIs%20v1.0%20Final.md
 */
class Tcf1GdprStrategy implements TcfGdprStrategy {

  @VisibleForTesting
  static final String IAB_CONSENT_STRING_KEY = "IABConsent_ConsentString";

  @VisibleForTesting
  static final String IAB_SUBJECT_TO_GDPR_KEY = "IABConsent_SubjectToGDPR";

  private final SafeSharedPreferences safeSharedPreferences;

  public Tcf1GdprStrategy(@NonNull SafeSharedPreferences safeSharedPreferences) {
    this.safeSharedPreferences = safeSharedPreferences;
  }

  @Override
  @NonNull
  public String getConsentString() {
    return safeSharedPreferences.getString(IAB_CONSENT_STRING_KEY, "");
  }

  @Override
  @NonNull
  public String getSubjectToGdpr() {
    return safeSharedPreferences.getString(IAB_SUBJECT_TO_GDPR_KEY, "");
  }

  @Override
  @NonNull
  public Integer getVersion() {
    return 1;
  }

  @Override
  public boolean isProvided() {
    String subjectToGdpr = getSubjectToGdpr();
    String consentString = getConsentString();
    boolean isSubjectToGdprEmpty = subjectToGdpr.isEmpty();
    boolean isConsentStringEmpty = consentString.isEmpty();

    return !isSubjectToGdprEmpty || !isConsentStringEmpty;
  }
}
