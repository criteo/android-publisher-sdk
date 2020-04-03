package com.criteo.publisher.util;

import com.criteo.publisher.BuildConfig;

public class LoggingUtil {

  public boolean isLoggingEnabled() {
    return BuildConfig.debugLogging;
  }
}
