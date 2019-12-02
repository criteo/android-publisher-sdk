package com.criteo.publisher;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.network.PubSdkApi;
import java.util.concurrent.Executor;

/**
 * Provides global dependencies to the rest of the codebase
 */
public class DependencyProvider {

  protected static DependencyProvider instance;

  private DependencyProvider() {
  }

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
  public AndroidUtil provideAndroidUtil(@NonNull Context context) {
    return new AndroidUtil(context);
  }

  @NonNull
  public DeviceUtil provideDeviceUtil(@NonNull Context context) {
    return new DeviceUtil(context, provideAdvertisingInfo());
  }

  @NonNull
  public LoggingUtil provideLoggingUtil() {
    return new LoggingUtil();
  }

  @NonNull
  public Executor provideThreadPoolExecutor() {
    return AsyncTask.THREAD_POOL_EXECUTOR;
  }

  @NonNull
  public Executor provideSerialExecutor() {
    return AsyncTask.SERIAL_EXECUTOR;
  }

  @NonNull
  public Config provideConfig(Context context) {
    return new Config(context);
  }
}
