package com.criteo.publisher.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.util.SafeSharedPreferences;
import com.criteo.publisher.privacy.gdpr.GdprData;
import com.criteo.publisher.privacy.gdpr.GdprDataFetcher;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// FIXME (ma.chentir) this class should be broken down into specific classes to handle each consent
//  mechanism separately https://jira.criteois.com/browse/EE-823
public class UserPrivacyUtil {

  // Regex according to the CCPA IAB String format defined in
  // https://iabtechlab.com/wp-content/uploads/2019/11/U.S.-Privacy-String-v1.0-IAB-Tech-Lab.pdf
  private static final Pattern IAB_USPRIVACY_PATTERN = Pattern.compile("^1(Y|N|-|y|n){3}$");

  // List of IAB Strings representing a positive consent
  private static final List<String> IAB_USPRIVACY_WITH_CONSENT = Arrays
      .asList("1ynn", "1yny", "1---", "", "1yn-", "1-n-");

  private static final List<String> MOPUB_CONSENT_DECLINED_STRINGS = Arrays
      .asList("explicit_no", "potential_whitelist", "dnt");

  // Key provided by the IAB CCPA Compliance Framework
  @VisibleForTesting
  static final String IAB_USPRIVACY_SHARED_PREFS_KEY = "IABUSPrivacy_String";

  // Storage key for the binary optout (for CCPA)
  @VisibleForTesting
  static final String OPTOUT_USPRIVACY_SHARED_PREFS_KEY = "USPrivacy_Optout";

  @VisibleForTesting
  static final String MOPUB_CONSENT_SHARED_PREFS_KEY = "MoPubConsent_String";

  private final SafeSharedPreferences safeSharedPreferences;

  private final SharedPreferences sharedPreferences;

  private GdprDataFetcher gdprDataFetcher;

  public UserPrivacyUtil(@NonNull Context context) {
    this(
        PreferenceManager.getDefaultSharedPreferences(context),
        new GdprDataFetcher(context)
    );
  }

  @VisibleForTesting
  UserPrivacyUtil(
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

  @NonNull
  public String getIabUsPrivacyString() {
    return safeSharedPreferences.getString(IAB_USPRIVACY_SHARED_PREFS_KEY, "");
  }

  public void storeUsPrivacyOptout(boolean uspOptout) {
    Editor edit = sharedPreferences.edit();
    edit.putString(OPTOUT_USPRIVACY_SHARED_PREFS_KEY, String.valueOf(uspOptout));
    edit.apply();
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
   * More information can be found here: https://confluence.criteois.com/display/PP/CCPA+Buying+Policy
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

  private boolean isBinaryConsentGiven() {
    String usPrivacyOptout = getUsPrivacyOptout();
    return Boolean.parseBoolean(usPrivacyOptout) != true;
  }

  private boolean isIABConsentGiven() {
    String iabUsPrivacy = getIabUsPrivacyString();

    return !IAB_USPRIVACY_PATTERN.matcher(iabUsPrivacy).matches() ||
        IAB_USPRIVACY_WITH_CONSENT.contains(iabUsPrivacy.toLowerCase(Locale.ROOT));
  }

  public boolean isMopubConsentGivenOrNotApplicable() {
    String mopubConsent = getMopubConsent();
    return !MOPUB_CONSENT_DECLINED_STRINGS.contains(mopubConsent.toLowerCase(Locale.ROOT));
  }

  public void storeMopubConsent(@Nullable String mopubConsent) {
    Editor edit = sharedPreferences.edit();
    edit.putString(MOPUB_CONSENT_SHARED_PREFS_KEY, mopubConsent);
    edit.apply();
  }

  @NonNull
  public String getMopubConsent() {
    return safeSharedPreferences.getString(MOPUB_CONSENT_SHARED_PREFS_KEY, "");
  }
}
