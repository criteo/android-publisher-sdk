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

package com.criteo.publisher.privacy;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.privacy.gdpr.GdprData;
import com.criteo.publisher.privacy.gdpr.GdprDataFetcher;
import com.criteo.publisher.util.SafeSharedPreferences;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class UserPrivacyUtil {

  // Regex according to the CCPA IAB String format defined in
  // https://iabtechlab.com/wp-content/uploads/2019/11/U.S.-Privacy-String-v1.0-IAB-Tech-Lab.pdf
  private static final Pattern IAB_USPRIVACY_PATTERN = Pattern.compile("^1([YN\\-yn]){3}$");

  // List of IAB Strings representing a positive consent
  private static final List<String> IAB_USPRIVACY_WITH_CONSENT = Arrays
      .asList("1ynn", "1yny", "1---", "", "1yn-", "1-n-");

  // Key provided by the IAB CCPA Compliance Framework
  @VisibleForTesting
  static final String IAB_USPRIVACY_SHARED_PREFS_KEY = "IABUSPrivacy_String";

  // Storage key for the binary optout (for CCPA)
  @VisibleForTesting
  static final String OPTOUT_USPRIVACY_SHARED_PREFS_KEY = "USPrivacy_Optout";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final SafeSharedPreferences safeSharedPreferences;

  private final SharedPreferences sharedPreferences;

  private final GdprDataFetcher gdprDataFetcher;

  @Nullable
  private Boolean tagForChildDirectedTreatment = null;

  public UserPrivacyUtil(
      @NonNull SharedPreferences sharedPreferences,
      @NonNull GdprDataFetcher gdprDataFetcher
  ) {
    this.sharedPreferences = sharedPreferences;
    this.safeSharedPreferences = new SafeSharedPreferences(sharedPreferences);
    this.gdprDataFetcher = gdprDataFetcher;
  }

  @Nullable
  public GdprData getGdprData() {
    return gdprDataFetcher.fetch();
  }

  @Nullable
  public String getGdprConsentData() {
    GdprData gdprData = gdprDataFetcher.fetch();
    if (gdprData == null) {
      return null;
    }
    return gdprData.getConsentData();
  }

  @NonNull
  public String getIabUsPrivacyString() {
    return safeSharedPreferences.getString(IAB_USPRIVACY_SHARED_PREFS_KEY, "");
  }

  public void storeUsPrivacyOptout(boolean uspOptout) {
    Editor edit = sharedPreferences.edit();
    edit.putString(OPTOUT_USPRIVACY_SHARED_PREFS_KEY, String.valueOf(uspOptout));
    edit.apply();
    logger.log(PrivacyLogMessage.onUsPrivacyOptOutSet(uspOptout));
  }

  @NonNull
  public String getUsPrivacyOptout() {
    return safeSharedPreferences.getString(OPTOUT_USPRIVACY_SHARED_PREFS_KEY, "");
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
   * <p>
   * More information can be found here: https://go.crto.in/publisher-sdk-ccpa
   *
   * @return {@code true} if consent is given, {@code false} otherwise
   */
  public boolean isCCPAConsentGivenOrNotApplicable() {
    String iabUsPrivacy = getIabUsPrivacyString();
    if (iabUsPrivacy.isEmpty()) {
      return isBinaryConsentGiven();
    }
    return isIABConsentGiven();
  }

  /**
   * Children’s Online Privacy Protection Act (“COPPA”) flag
   *
   * @return tag set for COPPA or {@code null} if it was never set
   */
  @Nullable
  public Boolean getTagForChildDirectedTreatment() {
    return tagForChildDirectedTreatment;
  }

  public void storeTagForChildDirectedTreatment(@Nullable Boolean flag) {
    tagForChildDirectedTreatment = flag;
  }

  private boolean isBinaryConsentGiven() {
    String usPrivacyOptout = getUsPrivacyOptout();
    return !Boolean.parseBoolean(usPrivacyOptout);
  }

  private boolean isIABConsentGiven() {
    String iabUsPrivacy = getIabUsPrivacyString();

    return !IAB_USPRIVACY_PATTERN.matcher(iabUsPrivacy).matches() ||
        IAB_USPRIVACY_WITH_CONSENT.contains(iabUsPrivacy.toLowerCase(Locale.ROOT));
  }
}
