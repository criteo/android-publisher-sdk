/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher;

import static java.util.Arrays.asList;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.advancednative.AdChoiceOverlay;
import com.criteo.publisher.advancednative.ClickDetection;
import com.criteo.publisher.advancednative.ClickHelper;
import com.criteo.publisher.advancednative.CriteoImageLoader;
import com.criteo.publisher.advancednative.ImageLoader;
import com.criteo.publisher.advancednative.ImageLoaderHolder;
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
import com.criteo.publisher.concurrent.AsyncResources;
import com.criteo.publisher.concurrent.NoOpAsyncResources;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import com.criteo.publisher.concurrent.ThreadPoolExecutorFactory;
import com.criteo.publisher.context.ConnectionTypeFetcher;
import com.criteo.publisher.context.ContextProvider;
import com.criteo.publisher.context.UserDataHolder;
import com.criteo.publisher.csm.ConcurrentSendingQueue;
import com.criteo.publisher.csm.CsmBidLifecycleListener;
import com.criteo.publisher.csm.MetricParser;
import com.criteo.publisher.csm.MetricRepository;
import com.criteo.publisher.csm.MetricRepositoryFactory;
import com.criteo.publisher.csm.MetricSendingQueue;
import com.criteo.publisher.csm.MetricSendingQueue.AdapterMetricSendingQueue;
import com.criteo.publisher.csm.MetricSendingQueueConfiguration;
import com.criteo.publisher.csm.MetricSendingQueueConsumer;
import com.criteo.publisher.csm.MetricSendingQueueProducer;
import com.criteo.publisher.csm.ObjectQueueFactory;
import com.criteo.publisher.csm.SendingQueueConfiguration;
import com.criteo.publisher.csm.SendingQueueFactory;
import com.criteo.publisher.dependency.LazyDependency;
import com.criteo.publisher.headerbidding.DfpHeaderBidding;
import com.criteo.publisher.headerbidding.HeaderBidding;
import com.criteo.publisher.headerbidding.MoPubHeaderBidding;
import com.criteo.publisher.headerbidding.OtherAdServersHeaderBidding;
import com.criteo.publisher.integration.IntegrationDetector;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.logging.ConsoleHandler;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.logging.PublisherCodeRemover;
import com.criteo.publisher.logging.RemoteHandler;
import com.criteo.publisher.logging.RemoteLogRecordsFactory;
import com.criteo.publisher.logging.RemoteLogSendingQueue;
import com.criteo.publisher.logging.RemoteLogSendingQueue.AdapterRemoteLogSendingQueue;
import com.criteo.publisher.logging.RemoteLogSendingQueueConfiguration;
import com.criteo.publisher.logging.RemoteLogSendingQueueConsumer;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import com.criteo.publisher.network.BidRequestSender;
import com.criteo.publisher.network.LiveBidRequestSender;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.privacy.ConsentData;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.AndroidUtil;
import com.criteo.publisher.util.AppLifecycleUtil;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.CustomAdapterFactory;
import com.criteo.publisher.util.DeviceUtil;
import com.criteo.publisher.util.JsonSerializer;
import com.criteo.publisher.util.MapUtilKt;
import com.criteo.publisher.util.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import kotlin.jvm.functions.Function0;

/**
 * Provides global dependencies to the rest of the codebase
 */
public class DependencyProvider {

  protected static DependencyProvider instance;

  private final ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<>();

  private Application application;
  private String criteoPublisherId;

  protected DependencyProvider() {
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

  boolean isApplicationSet() {
    try {
      DependencyProvider.getInstance().checkApplicationIsSet();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private void checkApplicationIsSet() {
    if (application == null) {
      throw new CriteoNotInitializedException("Application reference is required");
    }
  }

  private void checkCriteoPublisherIdIsSet() {
    if (TextUtils.isEmpty(criteoPublisherId)) {
      throw new CriteoNotInitializedException("Criteo Publisher Id is required");
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
    return getOrCreate(PubSdkApi.class, () -> new PubSdkApi(
        provideBuildConfigWrapper(),
        provideJsonSerializer()
    ));
  }

  @NonNull
  public AdvertisingInfo provideAdvertisingInfo() {
    return getOrCreate(AdvertisingInfo.class, () -> new AdvertisingInfo(
        provideContext(),
        provideThreadPoolExecutor()
    ));
  }

  @NonNull
  public AndroidUtil provideAndroidUtil() {
    return getOrCreate(AndroidUtil.class, () -> new AndroidUtil(
        provideContext(),
        provideDeviceUtil()
    ));
  }

  @NonNull
  public DeviceUtil provideDeviceUtil() {
    return getOrCreate(DeviceUtil.class, () -> new DeviceUtil(
        provideContext()
    ));
  }

  @NonNull
  public Executor provideThreadPoolExecutor() {
    return getOrCreate(ThreadPoolExecutor.class, new ThreadPoolExecutorFactory());
  }

  @NonNull
  public ScheduledExecutorService provideScheduledExecutorService() {
    return getOrCreate(ScheduledExecutorService.class, Executors::newSingleThreadScheduledExecutor);
  }

  @NonNull
  public RunOnUiThreadExecutor provideRunOnUiThreadExecutor() {
    return getOrCreate(RunOnUiThreadExecutor.class, RunOnUiThreadExecutor::new);
  }

  @NonNull
  public Config provideConfig() {
    return getOrCreate(Config.class, () -> new Config(
        provideSharedPreferences(),
        provideJsonSerializer()
    ));
  }

  @NonNull
  public Clock provideClock() {
    return getOrCreate(Clock.class, EpochClock::new);
  }

  @NonNull
  public UserPrivacyUtil provideUserPrivacyUtil() {
    return getOrCreate(UserPrivacyUtil.class, () -> new UserPrivacyUtil(
        provideContext()
    ));
  }

  @NonNull
  public BidManager provideBidManager() {
    return getOrCreate(BidManager.class, () -> new BidManager(
        new SdkCache(provideDeviceUtil()),
        provideConfig(),
        provideClock(),
        provideAdUnitMapper(),
        provideBidRequestSender(),
        provideLiveBidRequestSender(),
        provideBidLifecycleListener(),
        provideMetricSendingQueueConsumer(),
        provideRemoteLogSendingQueueConsumer(),
        provideConsentData()
    ));
  }

  @NonNull
  public DeviceInfo provideDeviceInfo() {
    return getOrCreate(DeviceInfo.class, () -> new DeviceInfo(
        provideContext(),
        provideRunOnUiThreadExecutor()
    ));
  }

  @NonNull
  public AdUnitMapper provideAdUnitMapper() {
    return getOrCreate(AdUnitMapper.class, () -> new AdUnitMapper(
        provideDeviceUtil()
    ));
  }

  @NonNull
  public AppEvents provideAppEvents() {
    return getOrCreate(AppEvents.class, () -> new AppEvents(
        provideContext(),
        provideAdvertisingInfo(),
        provideClock(),
        providePubSdkApi(),
        provideUserPrivacyUtil(),
        provideDeviceInfo(),
        provideThreadPoolExecutor()
    ));
  }

  @NonNull
  public AppLifecycleUtil provideAppLifecycleUtil() {
    return getOrCreate(AppLifecycleUtil.class, () -> new AppLifecycleUtil(
        provideAppEvents(),
        provideBidManager()
    ));
  }

  @NonNull
  public BuildConfigWrapper provideBuildConfigWrapper() {
    return getOrCreate(BuildConfigWrapper.class, BuildConfigWrapper::new);
  }

  @NonNull
  public CdbRequestFactory provideCdbRequestFactory() {
    return getOrCreate(CdbRequestFactory.class, () -> new CdbRequestFactory(
        provideContext(),
        provideCriteoPublisherId(),
        provideDeviceInfo(),
        provideAdvertisingInfo(),
        provideUserPrivacyUtil(),
        provideUniqueIdGenerator(),
        provideBuildConfigWrapper(),
        provideIntegrationRegistry(),
        provideContextProvider(),
        provideUserDataHolder()
    ));
  }

  @NonNull
  public UniqueIdGenerator provideUniqueIdGenerator() {
    return getOrCreate(UniqueIdGenerator.class, () -> new UniqueIdGenerator(
        provideClock()
    ));
  }

  @NonNull
  public RemoteConfigRequestFactory provideRemoteConfigRequestFactory() {
    return getOrCreate(RemoteConfigRequestFactory.class, () -> new RemoteConfigRequestFactory(
        provideContext(),
        provideCriteoPublisherId(),
        provideBuildConfigWrapper(),
        provideIntegrationRegistry(),
        provideAdvertisingInfo()
    ));
  }

  @NonNull
  public BidRequestSender provideBidRequestSender() {
    return getOrCreate(BidRequestSender.class, () -> new BidRequestSender(
        provideCdbRequestFactory(),
        provideRemoteConfigRequestFactory(),
        provideClock(),
        providePubSdkApi(),
        provideThreadPoolExecutor()
    ));
  }

  @NonNull
  public LiveBidRequestSender provideLiveBidRequestSender() {
    return getOrCreate(LiveBidRequestSender.class, () -> new LiveBidRequestSender(
        providePubSdkApi(),
        provideCdbRequestFactory(),
        provideClock(),
        provideThreadPoolExecutor(),
        provideScheduledExecutorService(),
        provideConfig()
    ));
  }

  @NonNull
  public BidLifecycleListener provideBidLifecycleListener() {
    return getOrCreate(BidLifecycleListener.class, () -> {
      CompositeBidLifecycleListener listener = new CompositeBidLifecycleListener();
      listener.add(new LoggingBidLifecycleListener(provideRemoteLogSendingQueueConsumer()));

      if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
        listener.add(new CsmBidLifecycleListener(
            provideMetricRepository(),
            provideMetricSendingQueueProducer(),
            provideClock(),
            provideConfig(),
            provideConsentData(),
            provideThreadPoolExecutor()
        ));
      }

      return listener;
    });
  }

  @NonNull
  public NativeAdMapper provideNativeAdMapper() {
    return getOrCreate(NativeAdMapper.class, () -> new NativeAdMapper(
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
    ));
  }

  @NonNull
  public VisibilityTracker provideVisibilityTracker() {
    return getOrCreate(VisibilityTracker.class, () -> new VisibilityTracker(
        new VisibilityChecker()
    ));
  }

  @NonNull
  public ClickDetection provideClickDetection() {
    return getOrCreate(ClickDetection.class, ClickDetection::new);
  }

  @NonNull
  public Redirection provideRedirection() {
    return getOrCreate(Redirection.class, () -> new Redirection(
        provideContext()
    ));
  }

  @NonNull
  public AdChoiceOverlay provideAdChoiceOverlay() {
    return getOrCreate(AdChoiceOverlay.class, () -> new AdChoiceOverlay(
        provideBuildConfigWrapper(),
        provideAndroidUtil()
    ));
  }

  @NonNull
  public Picasso providePicasso() {
    return getOrCreate(Picasso.class, () -> new Picasso.Builder(provideContext()).build());
  }

  @NonNull
  public ImageLoader provideDefaultImageLoader() {
    return getOrCreate(ImageLoader.class, () -> new CriteoImageLoader(
        providePicasso(),
        provideAsyncResources()
    ));
  }

  @NonNull
  public ImageLoaderHolder provideImageLoaderHolder() {
    return getOrCreate(ImageLoaderHolder.class, () -> new ImageLoaderHolder(provideDefaultImageLoader()));
  }

  @NonNull
  public RendererHelper provideRendererHelper() {
    return getOrCreate(RendererHelper.class, () -> new RendererHelper(
        provideImageLoaderHolder(),
        provideRunOnUiThreadExecutor()
    ));
  }

  @NonNull
  public AsyncResources provideAsyncResources() {
    return getOrCreate(AsyncResources.class, NoOpAsyncResources::new);
  }

  @NonNull
  public SharedPreferences provideSharedPreferences() {
    return getOrCreate(SharedPreferences.class, () -> provideContext().getSharedPreferences(
        BuildConfig.pubSdkSharedPreferences,
        Context.MODE_PRIVATE
    ));
  }

  @NonNull
  public IntegrationRegistry provideIntegrationRegistry() {
    return getOrCreate(IntegrationRegistry.class, () -> new IntegrationRegistry(
        provideSharedPreferences(),
        provideIntegrationDetector()
    ));
  }

  @NonNull
  public IntegrationDetector provideIntegrationDetector() {
    return getOrCreate(IntegrationDetector.class, IntegrationDetector::new);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getOrCreate(Class<T> klass, Factory<T> factory) {
    Object service = MapUtilKt.getOrCompute(services, klass, (Function0<T>) factory::create);

    // safe because the services map is only filled there by typed factory
    return (T) service;
  }

  @NonNull
  public ConsumableBidLoader provideConsumableBidLoader() {
    return getOrCreate(ConsumableBidLoader.class, () -> new ConsumableBidLoader(
        provideBidManager(),
        provideClock(),
        provideRunOnUiThreadExecutor()
    ));
  }

  @NonNull
  public HeaderBidding provideHeaderBidding() {
    return getOrCreate(HeaderBidding.class, () -> new HeaderBidding(
        asList(
            new MoPubHeaderBidding(),
            new DfpHeaderBidding(provideAndroidUtil(), provideDeviceUtil()),
            new OtherAdServersHeaderBidding()
        ),
        provideIntegrationRegistry()
    ));
  }

  @NonNull
  public InterstitialActivityHelper provideInterstitialActivityHelper() {
    return getOrCreate(InterstitialActivityHelper.class, () -> new InterstitialActivityHelper(
        provideContext(),
        provideTopActivityFinder()
    ));
  }

  @NonNull
  public TopActivityFinder provideTopActivityFinder() {
    return getOrCreate(TopActivityFinder.class, () -> new TopActivityFinder(
        provideContext()
    ));
  }

  @NonNull
  public MetricSendingQueueConsumer provideMetricSendingQueueConsumer() {
    return getOrCreate(MetricSendingQueueConsumer.class, () -> new MetricSendingQueueConsumer(
        provideMetricSendingQueue(),
        providePubSdkApi(),
        provideBuildConfigWrapper(),
        provideConfig(),
        provideThreadPoolExecutor()
    ));
  }

  @NonNull
  public MetricSendingQueueProducer provideMetricSendingQueueProducer() {
    return getOrCreate(MetricSendingQueueProducer.class, () -> new MetricSendingQueueProducer(
        provideMetricSendingQueue()
    ));
  }


  @NonNull
  public MetricSendingQueue provideMetricSendingQueue() {
    return getOrCreate(MetricSendingQueue.class, () -> new AdapterMetricSendingQueue(
        provideSendingQueue(provideMetricSendingQueueConfiguration())
    ));
  }

  private <T> ConcurrentSendingQueue<T> provideSendingQueue(SendingQueueConfiguration<T> configuration) {
    return new SendingQueueFactory<>(
        new ObjectQueueFactory<>(
            provideContext(),
            provideJsonSerializer(),
            configuration
        ),
        configuration
    ).create();
  }

  @NonNull
  public MetricSendingQueueConfiguration provideMetricSendingQueueConfiguration() {
    return getOrCreate(MetricSendingQueueConfiguration.class, () -> new MetricSendingQueueConfiguration(
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
    return getOrCreate(MetricParser.class, () -> new MetricParser(
        provideJsonSerializer()
    ));
  }

  @NonNull
  public JsonSerializer provideJsonSerializer() {
    return getOrCreate(JsonSerializer.class, () -> new JsonSerializer(
        provideGson()
    ));
  }

  @NonNull
  public Gson provideGson() {
    return getOrCreate(Gson.class, () -> new GsonBuilder()
        .registerTypeAdapterFactory(CustomAdapterFactory.create())
        .create());
  }

  @NonNull
  public LoggerFactory provideLoggerFactory() {
    return getOrCreate(LoggerFactory.class, () -> new LoggerFactory(Arrays.asList(
        new LazyDependency<>("ConsoleHandler", this::provideConsoleHandler),
        new LazyDependency<>("RemoteHandler", this::provideRemoteHandler)
    )));
  }

  @NonNull
  public ConsoleHandler provideConsoleHandler() {
    return getOrCreate(ConsoleHandler.class, () -> new ConsoleHandler(
        provideBuildConfigWrapper()
    ));
  }

  @NonNull
  public ContextProvider provideContextProvider() {
    return getOrCreate(ContextProvider.class, () -> new ContextProvider(
        provideContext(),
        provideConnectionTypeFetcher(),
        provideAndroidUtil(),
        provideSession()
    ));
  }

  @NonNull
  public ConnectionTypeFetcher provideConnectionTypeFetcher() {
    return getOrCreate(ConnectionTypeFetcher.class, () -> new ConnectionTypeFetcher(
        provideContext()
    ));
  }

  @NonNull
  public Session provideSession() {
    return getOrCreate(Session.class, () -> new Session(
        provideClock(),
        provideUniqueIdGenerator()
    ));
  }

  @NonNull
  public UserDataHolder provideUserDataHolder() {
    return getOrCreate(UserDataHolder.class, UserDataHolder::new);
  }

  @NonNull
  public RemoteLogSendingQueue provideRemoteLogSendingQueue() {
    return getOrCreate(RemoteLogSendingQueue.class, () -> new AdapterRemoteLogSendingQueue(
        provideSendingQueue(provideRemoteLogSendingQueueConfiguration())
    ));
  }

  @NonNull
  public RemoteLogSendingQueueConfiguration provideRemoteLogSendingQueueConfiguration() {
    return getOrCreate(RemoteLogSendingQueueConfiguration.class, () -> new RemoteLogSendingQueueConfiguration(
        provideBuildConfigWrapper()
    ));
  }

  @NonNull
  public RemoteLogRecordsFactory provideRemoteLogRecordsFactory() {
    return getOrCreate(RemoteLogRecordsFactory.class, () -> new RemoteLogRecordsFactory(
        provideBuildConfigWrapper(),
        provideContext(),
        provideAdvertisingInfo(),
        provideSession(),
        provideIntegrationRegistry(),
        provideClock(),
        providePublisherCodeRemover()
    ));
  }

  @NonNull
  public PublisherCodeRemover providePublisherCodeRemover() {
    return getOrCreate(PublisherCodeRemover.class, PublisherCodeRemover::new);
  }

  @NonNull
  public RemoteHandler provideRemoteHandler() {
    return getOrCreate(RemoteHandler.class, () -> new RemoteHandler(
        provideRemoteLogRecordsFactory(),
        provideRemoteLogSendingQueue(),
        provideConfig(),
        provideThreadPoolExecutor(),
        provideConsentData()
    ));
  }

  @NonNull
  public RemoteLogSendingQueueConsumer provideRemoteLogSendingQueueConsumer() {
    return getOrCreate(RemoteLogSendingQueueConsumer.class, () -> new RemoteLogSendingQueueConsumer(
        provideRemoteLogSendingQueue(),
        providePubSdkApi(),
        provideBuildConfigWrapper(),
        provideThreadPoolExecutor()
    ));
  }

  @NonNull
  public ConsentData provideConsentData() {
    return getOrCreate(ConsentData.class, () -> new ConsentData(provideSharedPreferences()));
  }

  public interface Factory<T> {

    @NonNull
    T create();
  }
}
