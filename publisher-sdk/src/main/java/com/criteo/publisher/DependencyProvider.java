package com.criteo.publisher;

import static java.util.Arrays.asList;

import android.app.Application;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.advancednative.AdChoiceOverlay;
import com.criteo.publisher.advancednative.ClickDetection;
import com.criteo.publisher.advancednative.ClickHelper;
import com.criteo.publisher.advancednative.CriteoImageLoader;
import com.criteo.publisher.advancednative.ImpressionHelper;
import com.criteo.publisher.advancednative.NativeAdMapper;
import com.criteo.publisher.advancednative.RendererHelper;
import com.criteo.publisher.advancednative.VisibilityChecker;
import com.criteo.publisher.advancednative.VisibilityTracker;
import com.criteo.publisher.adview.Redirection;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.bid.CompositeBidLifecycleListener;
import com.criteo.publisher.bid.LoggingBidLifecycleListener;
import com.criteo.publisher.bid.UniqueIdGenerator;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.concurrent.ThreadPoolExecutorFactory;
import com.criteo.publisher.csm.CsmBidLifecycleListener;
import com.criteo.publisher.csm.MetricObjectQueueFactory;
import com.criteo.publisher.csm.MetricParser;
import com.criteo.publisher.csm.MetricRepository;
import com.criteo.publisher.csm.MetricRepositoryFactory;
import com.criteo.publisher.csm.MetricSendingQueue;
import com.criteo.publisher.csm.MetricSendingQueueConsumer;
import com.criteo.publisher.csm.MetricSendingQueueFactory;
import com.criteo.publisher.csm.MetricSendingQueueProducer;
import com.criteo.publisher.headerbidding.DfpHeaderBidding;
import com.criteo.publisher.headerbidding.HeaderBidding;
import com.criteo.publisher.headerbidding.MoPubHeaderBidding;
import com.criteo.publisher.headerbidding.OtherAdServersHeaderBidding;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import com.criteo.publisher.network.BidRequestSender;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.CustomAdapterFactory;
import com.criteo.publisher.util.DeviceUtil;
import com.criteo.publisher.util.JsonSerializer;
import com.criteo.publisher.util.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

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

  /**KEEP VISIBILITY AS PACKAGE-PRIVATE **/
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
  public Executor provideThreadPoolExecutor() {
    return getOrCreate(ThreadPoolExecutor.class, new ThreadPoolExecutorFactory());
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
        return new Config(
            provideContext(),
            provideJsonSerializer()
        );
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
        CompositeBidLifecycleListener listener = new CompositeBidLifecycleListener();
        listener.add(new LoggingBidLifecycleListener());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
          listener.add(new CsmBidLifecycleListener(
              provideMetricRepository(),
              new MetricSendingQueueProducer(provideMetricSendingQueue()),
              provideClock(),
              provideUniqueIdGenerator(),
              provideConfig(),
              provideThreadPoolExecutor()
          ));
        }

        return listener;
      }
    });
  }

  @NonNull
  public NativeAdMapper provideNativeAdMapper() {
    return getOrCreate(NativeAdMapper.class, new Factory<NativeAdMapper>() {
      @NonNull
      @Override
      public NativeAdMapper create() {
        return new NativeAdMapper(
            provideVisibilityTracker(),
            new ImpressionHelper(
                providePubSdkApi(),
                provideThreadPoolExecutor(),
                provideRunOnUiThreadExecutor()
            ),
            provideClickDetection(),
            new ClickHelper(
                provideRedirection(),
                provideTopActivityFinder(),
                provideRunOnUiThreadExecutor()
            ),
            provideAdChoiceOverlay(),
            provideRendererHelper()
        );
      }
    });
  }

  @NonNull
  public VisibilityTracker provideVisibilityTracker() {
    return getOrCreate(VisibilityTracker.class, new Factory<VisibilityTracker>() {
      @NonNull
      @Override
      public VisibilityTracker create() {
        return new VisibilityTracker(new VisibilityChecker());
      }
    });
  }

  @NonNull
  public ClickDetection provideClickDetection() {
    return getOrCreate(ClickDetection.class, new Factory<ClickDetection>() {
      @NonNull
      @Override
      public ClickDetection create() {
        return new ClickDetection();
      }
    });
  }

  @NonNull
  public Redirection provideRedirection() {
    return getOrCreate(Redirection.class, new Factory<Redirection>() {
      @NonNull
      @Override
      public Redirection create() {
        return new Redirection(provideContext());
      }
    });
  }

  @NonNull
  public AdChoiceOverlay provideAdChoiceOverlay() {
    return getOrCreate(AdChoiceOverlay.class, new Factory<AdChoiceOverlay>() {
      @NonNull
      @Override
      public AdChoiceOverlay create() {
        return new AdChoiceOverlay(
            provideBuildConfigWrapper(),
            provideAndroidUtil()
        );
      }
    });
  }

  @NonNull
  public RendererHelper provideRendererHelper() {
    return getOrCreate(RendererHelper.class, new Factory<RendererHelper>() {
      @NonNull
      @Override
      public RendererHelper create() {
        return new RendererHelper(
            new CriteoImageLoader(),
            provideRunOnUiThreadExecutor()
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
  public HeaderBidding provideHeaderBidding() {
    return getOrCreate(HeaderBidding.class, new Factory<HeaderBidding>() {
      @NonNull
      @Override
      public HeaderBidding create() {
        return new HeaderBidding(
            provideBidManager(),
            asList(
                new MoPubHeaderBidding(),
                new DfpHeaderBidding(provideAndroidUtil(), provideDeviceUtil()),
                new OtherAdServersHeaderBidding()
            )
        );
      }
    });
  }

  @NonNull
  public InterstitialActivityHelper provideInterstitialActivityHelper() {
    return getOrCreate(InterstitialActivityHelper.class, new Factory<InterstitialActivityHelper>() {
      @NonNull
      @Override
      public InterstitialActivityHelper create() {
        return new InterstitialActivityHelper(
            provideContext(),
            provideTopActivityFinder()
        );
      }
    });
  }

  @NonNull
  public TopActivityFinder provideTopActivityFinder() {
    return getOrCreate(TopActivityFinder.class, new Factory<TopActivityFinder>() {
      @NonNull
      @Override
      public TopActivityFinder create() {
        return new TopActivityFinder(provideContext());
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
  public MetricObjectQueueFactory provideObjectQueueFactory() {
    return getOrCreate(MetricObjectQueueFactory.class, new Factory<MetricObjectQueueFactory>() {
      @NonNull
      @Override
      public MetricObjectQueueFactory create() {
        return new MetricObjectQueueFactory(
            provideContext(),
            provideMetricParser(),
            provideBuildConfigWrapper()
        );
      }
    });
  }

  @NonNull
  public MetricSendingQueue provideMetricSendingQueue() {
    return getOrCreate(MetricSendingQueue.class, new MetricSendingQueueFactory(
        provideObjectQueueFactory(),
        provideBuildConfigWrapper()
    ));
  }

  @NonNull
  @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
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

  @NonNull
  public LoggerFactory provideLoggerFactory() {
    return getOrCreate(LoggerFactory.class, new Factory<LoggerFactory>() {
      @NonNull
      @Override
      public LoggerFactory create() {
        return new LoggerFactory(provideBuildConfigWrapper());
      }
    });
  }

  public interface Factory<T> {
    @NonNull
    T create();
  }
}
