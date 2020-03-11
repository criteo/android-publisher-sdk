package com.criteo.publisher.Util;

import com.criteo.publisher.BuildConfig;

public class LoggingUtil {

  public boolean isLoggingEnabled() {
    return BuildConfig.DEBUG_LOGGING;
  }
}
