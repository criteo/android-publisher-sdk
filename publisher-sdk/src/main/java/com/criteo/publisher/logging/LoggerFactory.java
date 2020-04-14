package com.criteo.publisher.logging;

import android.support.annotation.NonNull;
import com.criteo.publisher.DependencyProvider;

public class LoggerFactory {

  @NonNull
  public static Logger getLogger(@NonNull Class<?> klass) {
    return DependencyProvider.getInstance().provideLoggerFactory().createLogger(klass);
  }

  public Logger createLogger(@NonNull Class<?> klass) {
    return new Logger(klass);
  }

}
