package com.criteo.publisher;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.Executor;

/**
 * Provides global dependencies to the rest of the codebase
 */
public class DependencyProvider {
  protected static DependencyProvider instance;

  private DependencyProvider() {}

  @NonNull
  public static synchronized DependencyProvider getInstance() {
    if (instance == null) {
      instance = new DependencyProvider();
    }
    return instance;
  }

  /** KEEP VISIBILITY AS PACKAGE-PRIVATE **/
  /**
   * This method will be used by tests to provide a fake {@link DependencyProvider} instance
   * @param dependencyProvider
   */
  static synchronized void setInstance(@NonNull DependencyProvider dependencyProvider) {
    instance = dependencyProvider;
  }

  @NonNull
  public PubSdkApi providePubSdkApi() {
    return PubSdkApi.getInstance();
  }

  @NonNull
  public AdvertisingInfo provideAdvertisingInfo() {
    return AdvertisingInfo.getInstance();
  }

  @NonNull
  public Executor provideThreadPoolExecutor() {
    return AsyncTask.THREAD_POOL_EXECUTOR;
  }

  @NonNull
  public Executor provideSerialExecutor() {
    return AsyncTask.SERIAL_EXECUTOR;
  }
}
