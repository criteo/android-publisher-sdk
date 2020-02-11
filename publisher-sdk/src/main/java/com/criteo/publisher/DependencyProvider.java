package com.criteo.publisher;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.RunOnUiThreadExecutor;
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
  static synchronized void setInstance(@Nullable DependencyProvider dependencyProvider) {
    instance = dependencyProvider;
  }

  @NonNull
  public PubSdkApi providePubSdkApi(Context context) {
    return getOrCreate(PubSdkApi.class, new Factory<PubSdkApi>() {
      @Override
      public PubSdkApi create() {
        return new PubSdkApi(context);
      }
    });
  }

  @NonNull
  public AdvertisingInfo provideAdvertisingInfo() {
    return getOrCreate(AdvertisingInfo.class, new Factory<AdvertisingInfo>() {
      @Override
      public AdvertisingInfo create() {
        return new AdvertisingInfo();
      }
    });
  }

  @NonNull
  public AndroidUtil provideAndroidUtil(@NonNull Context context) {
    return getOrCreate(AndroidUtil.class, new Factory<AndroidUtil>() {
      @Override
      public AndroidUtil create() {
        return new AndroidUtil(context);
      }
    });
  }

  @NonNull
  public DeviceUtil provideDeviceUtil(@NonNull Context context) {
    return getOrCreate(DeviceUtil.class, new Factory<DeviceUtil>() {
      @Override
      public DeviceUtil create() {
        return new DeviceUtil(context, DependencyProvider.this.provideAdvertisingInfo());
      }
    });
  }

  @NonNull
  public LoggingUtil provideLoggingUtil() {
    return getOrCreate(LoggingUtil.class, new Factory<LoggingUtil>() {
      @Override
      public LoggingUtil create() {
        return new LoggingUtil();
      }
    });
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
  public RunOnUiThreadExecutor provideRunOnUiThreadExecutor() {
    return getOrCreate(RunOnUiThreadExecutor.class, new Factory<RunOnUiThreadExecutor>() {
      @Override
      public RunOnUiThreadExecutor create() {
        return new RunOnUiThreadExecutor();
      }
    });
  }

  @NonNull
  public Config provideConfig(Context context) {
    return getOrCreate(Config.class, new Factory<Config>() {
      @Override
      public Config create() {
        return new Config(context);
      }
    });
  }

  @NonNull
  public Clock provideClock() {
    return getOrCreate(Clock.class, new Factory<Clock>() {
      @Override
      public Clock create() {
        return new EpochClock();
      }
    });
  }

  @NonNull
  public UserPrivacyUtil provideUserPrivacyUtil(@NonNull Context context) {
    return getOrCreate(UserPrivacyUtil.class, new Factory<UserPrivacyUtil>() {
      @Override
      public UserPrivacyUtil create() {
        return new UserPrivacyUtil(context);
      }
    });
  }

  @NonNull
  public BidManager provideBidManager(
      @NonNull Context context,
      @NonNull String criteoPublisherId) {
    return getOrCreate(BidManager.class, new Factory<BidManager>() {
      @Override
      public BidManager create() {
        return new BidManager(
            new Publisher(context, criteoPublisherId),
            new TokenCache(),
            DependencyProvider.this.provideDeviceInfo(context),
            DependencyProvider.this.provideUser(context),
            new SdkCache(DependencyProvider.this.provideDeviceUtil(context)),
            new Hashtable<>(),
            DependencyProvider.this.provideConfig(context),
            DependencyProvider.this.provideDeviceUtil(context),
            DependencyProvider.this.provideLoggingUtil(),
            DependencyProvider.this.provideClock(),
            DependencyProvider.this.provideUserPrivacyUtil(context),
            DependencyProvider.this.provideAdUnitMapper(context),
            DependencyProvider.this.providePubSdkApi(context)
        );
      }
    });
  }

  @NonNull
  public User provideUser(@NonNull Context context) {
    return getOrCreate(User.class, new Factory<User>() {
      @Override
      public User create() {
        return new User(DependencyProvider.this.provideDeviceUtil(context));
      }
    });
  }

  @NonNull
  public DeviceInfo provideDeviceInfo(Context context) {
    return getOrCreate(DeviceInfo.class, new Factory<DeviceInfo>() {
      @Override
      public DeviceInfo create() {
        return new DeviceInfo(
            context,
            DependencyProvider.this.provideRunOnUiThreadExecutor());
      }
    });
  }

  @NonNull
  public AdUnitMapper provideAdUnitMapper(Context context) {
    return getOrCreate(AdUnitMapper.class, new Factory<AdUnitMapper>() {
      @Override
      public AdUnitMapper create() {
        return new AdUnitMapper(
            DependencyProvider.this.provideAndroidUtil(context),
            DependencyProvider.this.provideDeviceUtil(context));
      }
    });
  }

  @NonNull
  public AppEvents provideAppEvents(@NonNull Context context) {
    return getOrCreate(AppEvents.class, new Factory<AppEvents>() {
      @Override
      public AppEvents create() {
        return new AppEvents(
            context,
            DependencyProvider.this.provideDeviceUtil(context),
            DependencyProvider.this.provideClock(),
            DependencyProvider.this.providePubSdkApi(context),
            DependencyProvider.this.provideUserPrivacyUtil(context),
            DependencyProvider.this.provideDeviceInfo(context)
        );
      }
    });
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
