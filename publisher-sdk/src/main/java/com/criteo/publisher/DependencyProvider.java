package com.criteo.publisher;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.CustomAdapterFactory;
import com.criteo.publisher.util.DeviceUtil;
import com.criteo.publisher.util.JsonSerializer;
import com.criteo.publisher.util.LoggingUtil;
import com.criteo.publisher.util.RunOnUiThreadExecutor;
import com.criteo.publisher.util.TextUtils;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.bid.CompositeBidLifecycleListener;
import com.criteo.publisher.bid.LoggingBidLifecycleListener;
import com.criteo.publisher.bid.UniqueIdGenerator;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.csm.CsmBidLifecycleListener;
import com.criteo.publisher.csm.MetricParser;
import com.criteo.publisher.csm.MetricRepository;
import com.criteo.publisher.csm.MetricRepositoryFactory;
import com.criteo.publisher.csm.MetricSendingQueue;
import com.criteo.publisher.csm.MetricSendingQueueConsumer;
import com.criteo.publisher.csm.MetricSendingQueueFactory;
import com.criteo.publisher.csm.MetricSendingQueueProducer;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import com.criteo.publisher.network.BidRequestSender;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Provides global dependencies to the rest of the codebase
 */
public class DependencyProvider {

  protected static DependencyProvider instance;

  private final Map<Class, Object> services = new HashMap<>();

  private Application application;
  private String criteoPublisherId;

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

  public void setApplication(@NonNull Application application) {
    this.application = application;
    checkApplicationIsSet();
  }

  public void setCriteoPublisherId(@NonNull String criteoPublisherId) {
    this.criteoPublisherId = criteoPublisherId;
    checkCriteoPublisherIdIsSet();
  }

  private void checkApplicationIsSet() {
    if (application == null) {
      throw new IllegalArgumentException("Application reference is required.");
    }
  }

  private void checkCriteoPublisherIdIsSet() {
    if (TextUtils.isEmpty(criteoPublisherId)) {
      throw new IllegalArgumentException("Criteo Publisher Id is required.");
    }
  }

  @NonNull
  public Application provideApplication() {
    checkApplicationIsSet();
    return application;
  }

  @NonNull
  public Context provideContext() {
    return provideApplication().getApplicationContext();
  }

  @NonNull
  public String provideCriteoPublisherId() {
    checkCriteoPublisherIdIsSet();
    return criteoPublisherId;
  }

  @NonNull
  public PubSdkApi providePubSdkApi() {
    return getOrCreate(PubSdkApi.class, new Factory<PubSdkApi>() {
      @NonNull
      @Override
      public PubSdkApi create() {
        return new PubSdkApi(
            provideBuildConfigWrapper(),
            provideJsonSerializer()
        );
      }
    });
  }

  @NonNull
  public AdvertisingInfo provideAdvertisingInfo() {
    return getOrCreate(AdvertisingInfo.class, new Factory<AdvertisingInfo>() {
      @NonNull
      @Override
      public AdvertisingInfo create() {
        return new AdvertisingInfo();
      }
    });
  }

  @NonNull
  public AndroidUtil provideAndroidUtil() {
    return getOrCreate(AndroidUtil.class, new Factory<AndroidUtil>() {
      @NonNull
      @Override
      public AndroidUtil create() {
        return new AndroidUtil(provideContext());
      }
    });
  }

  @NonNull
  public DeviceUtil provideDeviceUtil() {
    return getOrCreate(DeviceUtil.class, new Factory<DeviceUtil>() {
      @NonNull
      @Override
      public DeviceUtil create() {
        return new DeviceUtil(
            provideContext(),
            provideAdvertisingInfo()
        );
      }
    });
  }

  @NonNull
  public LoggingUtil provideLoggingUtil() {
    return getOrCreate(LoggingUtil.class, new Factory<LoggingUtil>() {
      @NonNull
      @Override
      public LoggingUtil create() {
        return new LoggingUtil();
      }
    });
  }

  @NonNull
  public Executor provideThreadPoolExecutor() {
    // FIXME EE-1006 Use dedicated executor service, see AndroidThreadPoolExecutorFactory
    return AsyncTask.THREAD_POOL_EXECUTOR;
  }

  @NonNull
  public Executor provideSerialExecutor() {
    return AsyncTask.SERIAL_EXECUTOR;
  }

  @NonNull
  public RunOnUiThreadExecutor provideRunOnUiThreadExecutor() {
    return getOrCreate(RunOnUiThreadExecutor.class, new Factory<RunOnUiThreadExecutor>() {
      @NonNull
      @Override
      public RunOnUiThreadExecutor create() {
        return new RunOnUiThreadExecutor();
      }
    });
  }

  @NonNull
  public Config provideConfig() {
    return getOrCreate(Config.class, new Factory<Config>() {
      @NonNull
      @Override
      public Config create() {
        return new Config(provideContext());
      }
    });
  }

  @NonNull
  public Clock provideClock() {
    return getOrCreate(Clock.class, new Factory<Clock>() {
      @NonNull
      @Override
      public Clock create() {
        return new EpochClock();
      }
    });
  }

  @NonNull
  public UserPrivacyUtil provideUserPrivacyUtil() {
    return getOrCreate(UserPrivacyUtil.class, new Factory<UserPrivacyUtil>() {
      @NonNull
      @Override
      public UserPrivacyUtil create() {
        return new UserPrivacyUtil(provideContext());
      }
    });
  }

  @NonNull
  public BidManager provideBidManager() {
    return getOrCreate(BidManager.class, new Factory<BidManager>() {
      @NonNull
      @Override
      public BidManager create() {
        return new BidManager(
            new SdkCache(provideDeviceUtil()),
            provideConfig(),
            provideClock(),
            provideAdUnitMapper(),
            provideBidRequestSender(),
            provideBidLifecycleListener(),
            provideMetricSendingQueueConsumer()
        );
      }
    });
  }

  @NonNull
  public DeviceInfo provideDeviceInfo() {
    return getOrCreate(DeviceInfo.class, new Factory<DeviceInfo>() {
      @NonNull
      @Override
      public DeviceInfo create() {
        return new DeviceInfo(
            provideContext(),
            provideRunOnUiThreadExecutor());
      }
    });
  }

  @NonNull
  public AdUnitMapper provideAdUnitMapper() {
    return getOrCreate(AdUnitMapper.class, new Factory<AdUnitMapper>() {
      @NonNull
      @Override
      public AdUnitMapper create() {
        return new AdUnitMapper(
            DependencyProvider.this.provideAndroidUtil(),
            DependencyProvider.this.provideDeviceUtil());
      }
    });
  }

  @NonNull
  public AppEvents provideAppEvents() {
    return getOrCreate(AppEvents.class, new Factory<AppEvents>() {
      @NonNull
      @Override
      public AppEvents create() {
        return new AppEvents(
            provideContext(),
            provideDeviceUtil(),
            provideClock(),
            providePubSdkApi(),
            provideUserPrivacyUtil(),
            provideDeviceInfo()
        );
      }
    });
  }

  @NonNull
  public Publisher providePublisher() {
    return getOrCreate(Publisher.class, new Factory<Publisher>() {
      @NonNull
      @Override
      public Publisher create() {
        return new Publisher(provideContext(), provideCriteoPublisherId());
      }
    });
  }

  @NonNull
  public BuildConfigWrapper provideBuildConfigWrapper() {
    return getOrCreate(BuildConfigWrapper.class, new Factory<BuildConfigWrapper>() {
      @NonNull
      @Override
      public BuildConfigWrapper create() {
        return new BuildConfigWrapper();
      }
    });
  }

  @NonNull
  public CdbRequestFactory provideCdbRequestFactory() {
    return getOrCreate(CdbRequestFactory.class, new Factory<CdbRequestFactory>() {
      @NonNull
      @Override
      public CdbRequestFactory create() {
        return new CdbRequestFactory(
            providePublisher(),
            provideDeviceInfo(),
            provideDeviceUtil(),
            provideUserPrivacyUtil(),
            provideUniqueIdGenerator(),
            provideBuildConfigWrapper()
        );
      }
    });
  }

  @NonNull
  public UniqueIdGenerator provideUniqueIdGenerator() {
    return getOrCreate(UniqueIdGenerator.class, new Factory<UniqueIdGenerator>() {
      @NonNull
      @Override
      public UniqueIdGenerator create() {
        return new UniqueIdGenerator(provideClock());
      }
    });
  }

  @NonNull
  public RemoteConfigRequestFactory provideRemoteConfigRequestFactory() {
    return getOrCreate(RemoteConfigRequestFactory.class, new Factory<RemoteConfigRequestFactory>() {
      @NonNull
      @Override
      public RemoteConfigRequestFactory create() {
        return new RemoteConfigRequestFactory(
            providePublisher(),
            provideBuildConfigWrapper()
        );
      }
    });
  }

  @NonNull
  public BidRequestSender provideBidRequestSender() {
    return getOrCreate(BidRequestSender.class, new Factory<BidRequestSender>() {
      @NonNull
      @Override
      public BidRequestSender create() {
        return new BidRequestSender(
            provideCdbRequestFactory(),
            provideRemoteConfigRequestFactory(),
            providePubSdkApi(),
            provideThreadPoolExecutor()
        );
      }
    });
  }

  @NonNull
  public BidLifecycleListener provideBidLifecycleListener() {
    return getOrCreate(BidLifecycleListener.class, new Factory<BidLifecycleListener>() {
      @NonNull
      @Override
      public BidLifecycleListener create() {
        BidLifecycleListener loggingListener = new LoggingBidLifecycleListener(provideLoggingUtil());

        BidLifecycleListener csmListener = new CsmBidLifecycleListener(
            provideMetricRepository(),
            new MetricSendingQueueProducer(provideMetricSendingQueue()),
            provideClock(),
            provideUniqueIdGenerator(),
            provideConfig()
        );

        return new CompositeBidLifecycleListener(loggingListener, csmListener);
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

  @NonNull
  public InHouse provideInHouse() {
    return getOrCreate(InHouse.class, new Factory<InHouse>() {
      @NonNull
      @Override
      public InHouse create() {
        return new InHouse(
            DependencyProvider.this.provideBidManager(),
            new TokenCache(),
            DependencyProvider.this.provideClock(),
            DependencyProvider.this.provideInterstitialActivityHelper());
      }
    });
  }

  @NonNull
  public InterstitialActivityHelper provideInterstitialActivityHelper() {
    return getOrCreate(InterstitialActivityHelper.class, new Factory<InterstitialActivityHelper>() {
      @NonNull
      @Override
      public InterstitialActivityHelper create() {
        return new InterstitialActivityHelper(provideContext());
      }
    });
  }

  @NonNull
  public MetricSendingQueueConsumer provideMetricSendingQueueConsumer() {
    return getOrCreate(MetricSendingQueueConsumer.class, new Factory<MetricSendingQueueConsumer>() {
      @NonNull
      @Override
      public MetricSendingQueueConsumer create() {
        return new MetricSendingQueueConsumer(
            provideMetricSendingQueue(),
            providePubSdkApi(),
            provideBuildConfigWrapper(),
            provideConfig(),
            provideThreadPoolExecutor()
        );
      }
    });
  }

  @NonNull
  public MetricSendingQueue provideMetricSendingQueue() {
    return getOrCreate(MetricSendingQueue.class, new MetricSendingQueueFactory(
        provideContext(),
        provideMetricParser(),
        provideBuildConfigWrapper()
    ));
  }

  @NonNull
  public MetricRepository provideMetricRepository() {
    return getOrCreate(MetricRepository.class, new MetricRepositoryFactory(
        provideContext(),
        provideMetricParser(),
        provideBuildConfigWrapper()
    ));
  }

  @NonNull
  public MetricParser provideMetricParser() {
    return getOrCreate(MetricParser.class, new Factory<MetricParser>() {
      @NonNull
      @Override
      public MetricParser create() {
        return new MetricParser(
            provideGson(),
            provideJsonSerializer()
        );
      }
    });
  }

  @NonNull
  public JsonSerializer provideJsonSerializer() {
    return getOrCreate(JsonSerializer.class, new Factory<JsonSerializer>() {
      @NonNull
      @Override
      public JsonSerializer create() {
        return new JsonSerializer(provideGson());
      }
    });
  }

  @NonNull
  public Gson provideGson() {
    return getOrCreate(Gson.class, new Factory<Gson>() {
      @NonNull
      @Override
      public Gson create() {
        return new GsonBuilder()
            .registerTypeAdapterFactory(CustomAdapterFactory.create())
            .create();
      }
    });
  }

  public interface Factory<T> {
    @NonNull
    T create();
  }

}
