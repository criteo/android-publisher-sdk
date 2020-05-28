package com.criteo.publisher.privacy.gdpr;

import android.content.Context;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.util.SafeSharedPreferences;

public class GdprDataFetcher {

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

    return GdprData.create(
        consentString,
        subjectToGdpr.isEmpty() ? null : "1".equals(subjectToGdpr),
        tcfStrategy.getVersion()
    );
  }
}
