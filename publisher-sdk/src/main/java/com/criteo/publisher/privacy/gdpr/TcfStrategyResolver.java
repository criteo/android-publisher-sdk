package com.criteo.publisher.privacy.gdpr;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.SafeSharedPreferences;

public class TcfStrategyResolver {
  private final SafeSharedPreferences safeSharedPreferences;

  public TcfStrategyResolver(@NonNull SafeSharedPreferences safeSharedPreferences) {
    this.safeSharedPreferences = safeSharedPreferences;
  }

  @Nullable
  TcfGdprStrategy resolveTcfStrategy() {
    Tcf2GdprStrategy tcf2GdprStrategy = new Tcf2GdprStrategy(safeSharedPreferences);

    if (tcf2GdprStrategy.isProvided()) {
      return tcf2GdprStrategy;
    }

    Tcf1GdprStrategy tcf1GdprStrategy = new Tcf1GdprStrategy(safeSharedPreferences);

    if (tcf1GdprStrategy.isProvided()) {
      return tcf1GdprStrategy;

    }

    return null;
  }
}
