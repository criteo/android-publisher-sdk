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
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/tree/master/TCFv2
 */
class Tcf2GdprStrategy implements TcfGdprStrategy {

  private static final int GDPR_APPLIES_UNSET = -1;

  @VisibleForTesting
  static final String IAB_TCString_Key = "IABTCF_TCString";

  @VisibleForTesting
  static final String IAB_GDPR_APPLIES_KEY = "IABTCF_gdprApplies";

  private final SafeSharedPreferences safeSharedPreferences;

  public Tcf2GdprStrategy(SafeSharedPreferences safeSharedPreferences) {
    this.safeSharedPreferences = safeSharedPreferences;
  }

  @Override
  @NonNull
  public String getConsentString() {
    return safeSharedPreferences.getString(IAB_TCString_Key, "");
  }

  @Override
  @NonNull
  public String getSubjectToGdpr() {
    int gdprApplies = safeSharedPreferences.getInt(IAB_GDPR_APPLIES_KEY, GDPR_APPLIES_UNSET);
    return gdprApplies != GDPR_APPLIES_UNSET ? String.valueOf(gdprApplies) : "";
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
    boolean isSubjectToGdprEmpty = subjectToGdpr.isEmpty();
    boolean isConsentStringEmpty = consentString.isEmpty();

    return !isSubjectToGdprEmpty || !isConsentStringEmpty;
  }
}
