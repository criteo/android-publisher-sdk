package com.criteo.publisher;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.PubSdkApi;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Provides global dependencies to the rest of the codebase
 */
public class DependencyProvider {

  protected static DependencyProvider instance;

  private final Map<Class, Object> services = new HashMap<>();

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
  public PubSdkApi providePubSdkApi(Context context) {
    return getOrCreate(PubSdkApi.class, () -> new PubSdkApi(context));
  }

  @NonNull
  public AdvertisingInfo provideAdvertisingInfo() {
    return getOrCreate(AdvertisingInfo.class, AdvertisingInfo::new);
  }

  @NonNull
  public AndroidUtil provideAndroidUtil(@NonNull Context context) {
    return getOrCreate(AndroidUtil.class, () -> new AndroidUtil(context));
  }

  @NonNull
  public DeviceUtil provideDeviceUtil(@NonNull Context context) {
    return getOrCreate(DeviceUtil.class, () -> new DeviceUtil(context, provideAdvertisingInfo()));
  }

  @NonNull
  public LoggingUtil provideLoggingUtil() {
    return getOrCreate(LoggingUtil.class, LoggingUtil::new);
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
    return getOrCreate(Config.class, () -> new Config(context));
  }

  @NonNull
  public Clock provideClock() {
    return getOrCreate(Clock.class, EpochClock::new);
  }

  @NonNull
  public UserPrivacyUtil provideUserPrivacyUtil(@NonNull Context context) {
    return getOrCreate(UserPrivacyUtil.class, () -> new UserPrivacyUtil(context));
  }

  @NonNull
  public BidManager provideBidManager(
      @NonNull Context context,
      @NonNull String criteoPublisherId) {
    return getOrCreate(BidManager.class, () -> new BidManager(
        new Publisher(context, criteoPublisherId),
        new TokenCache(),
        provideDeviceInfo(),
        new User(provideDeviceUtil(context)),
        new SdkCache(provideDeviceUtil(context)),
        new Hashtable<>(),
        provideConfig(context),
        provideDeviceUtil(context),
        provideLoggingUtil(),
        provideClock(),
        provideUserPrivacyUtil(context),
        provideAdUnitMapper(context),
        providePubSdkApi(context)
    ));
  }

  @NonNull
  public DeviceInfo provideDeviceInfo() {
    return getOrCreate(DeviceInfo.class, DeviceInfo::new);
  }

  @NonNull
  public AdUnitMapper provideAdUnitMapper(Context context) {
    return getOrCreate(AdUnitMapper.class, () -> new AdUnitMapper(
        provideAndroidUtil(context),
        provideDeviceUtil(context)));
  }

  @SuppressWarnings("unchecked")
  private <T> T getOrCreate(Class<T> klass, Factory<T> factory) {
    Object service = services.get(klass);
    if (service != null) {
      // safe because the services map is only filled there by typed factory
      return (T) service;
    }

    T newService = factory.create();
    services.put(klass, newService);
    return newService;
  }

  private interface Factory<T> {
    T create();
  }

}
