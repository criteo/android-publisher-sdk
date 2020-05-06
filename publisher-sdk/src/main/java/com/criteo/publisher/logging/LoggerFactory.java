package com.criteo.publisher.logging;

import android.support.annotation.NonNull;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.util.BuildConfigWrapper;

public class LoggerFactory {

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public LoggerFactory(@NonNull BuildConfigWrapper buildConfigWrapper) {
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  public static Logger getLogger(@NonNull Class<?> klass) {
    return DependencyProvider.getInstance().provideLoggerFactory().createLogger(klass);
  }

  public Logger createLogger(@NonNull Class<?> klass) {
    return new Logger(klass, buildConfigWrapper);
  }

}
