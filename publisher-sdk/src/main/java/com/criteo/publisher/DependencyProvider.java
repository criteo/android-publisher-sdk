package com.criteo.publisher;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.PubSdkApi;
import java.util.Hashtable;
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
    return new PubSdkApi();
  }

  @NonNull
  public AdvertisingInfo provideAdvertisingInfo() {
    return new AdvertisingInfo();
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

  @NonNull
  public Clock provideClock() {
    return new EpochClock();
  }

  @NonNull
  public UserPrivacyUtil provideUserPrivacyUtil(@NonNull Context context) {
    return new UserPrivacyUtil(context);
  }

  @NonNull
  public BidManager provideBidManager(
      @NonNull Context context,
      @NonNull String criteoPublisherId,
      @NonNull DeviceInfo deviceInfo,
      @NonNull Config config,
      @NonNull DeviceUtil deviceUtil,
      @NonNull LoggingUtil loggingUtil,
      @NonNull Clock clock,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull AdUnitMapper adUnitMapper) {
    return new BidManager(
        context,
        new Publisher(context, criteoPublisherId),
        new TokenCache(),
        deviceInfo,
        new User(deviceUtil),
        new SdkCache(deviceUtil),
        new Hashtable<>(),
        config,
        deviceUtil,
        loggingUtil,
        clock,
        userPrivacyUtil,
        adUnitMapper
    );
  }

  @NonNull
  public DeviceInfo provideDeviceInfo() {
    return new DeviceInfo();
  }

  public AdUnitMapper provideAdUnitMapper(AndroidUtil androidUtil, DeviceUtil deviceUtil) {
    return new AdUnitMapper(androidUtil, deviceUtil);
  }
}
