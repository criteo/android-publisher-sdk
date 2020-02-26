package com.criteo.publisher.privacy.gdpr;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TcfStrategyResolver {
  private final SharedPreferences sharedPreferences;

  public TcfStrategyResolver(@NonNull SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  @Nullable
  TcfGdprStrategy resolveTcfStrategy() {
    Tcf2GdprStrategy tcf2GdprStrategy = new Tcf2GdprStrategy(sharedPreferences);

    if (tcf2GdprStrategy.isProvided()) {
      return tcf2GdprStrategy;
    }

    Tcf1GdprStrategy tcf1GdprStrategy = new Tcf1GdprStrategy(sharedPreferences);

    if (tcf1GdprStrategy.isProvided()) {
      return tcf1GdprStrategy;

    }

    return null;
  }
}
