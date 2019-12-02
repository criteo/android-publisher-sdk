package com.criteo.publisher.Util;

import android.text.TextUtils;

public class LoggingUtil {
  private static final String CRITEO_LOGGING = "CRITEO_LOGGING";

  public boolean isLoggingEnabled() {
    String log = System.getenv(CRITEO_LOGGING);
    return TextUtils.isEmpty(log) ? false : Boolean.parseBoolean(log);
  }
}
