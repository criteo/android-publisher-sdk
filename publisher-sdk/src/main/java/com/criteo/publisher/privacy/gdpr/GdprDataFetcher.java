package com.criteo.publisher.privacy.gdpr;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.Util.SafeSharedPreferences;

public class GdprDataFetcher {

  /**
   * Criteo's vendor ID. Note that Criteo's ID is 91, however the vendor consents string is zero based
   *
   * @see <a href="https://vendorlist.consensu.org/vendorlist.json">Vendor list</a>
   * @see <a href="">https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#what-is-the-global-vendor-list</a>
   */
  private static final int ZERO_BASED_CRITEO_VENDOR_ID = 90;

  @NonNull
  private final TcfStrategyResolver tcfStrategyResolver;

  public GdprDataFetcher(@NonNull Context context) {
    this(
        new TcfStrategyResolver(
            new SafeSharedPreferences(PreferenceManager.getDefaultSharedPreferences(context))
        )
    );
  }

  @VisibleForTesting
  GdprDataFetcher(@NonNull TcfStrategyResolver tcfStrategyResolver) {
    this.tcfStrategyResolver = tcfStrategyResolver;
  }

  @Nullable
  public GdprData fetch() {
    TcfGdprStrategy tcfStrategy = tcfStrategyResolver.resolveTcfStrategy();

    if (tcfStrategy == null) {
      return null;
    }

    String subjectToGdpr = tcfStrategy.getSubjectToGdpr();
    String consentString = tcfStrategy.getConsentString();
    String vendorConsents = tcfStrategy.getVendorConsents();

    return GdprData.create(
        (vendorConsents.length() > ZERO_BASED_CRITEO_VENDOR_ID
            && vendorConsents.charAt(ZERO_BASED_CRITEO_VENDOR_ID) == '1'),
        consentString,
        "1".equals(subjectToGdpr),
        tcfStrategy.getVersion()
    );
  }
}
