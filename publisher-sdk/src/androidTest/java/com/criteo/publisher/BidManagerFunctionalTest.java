package com.criteo.publisher;

import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.PubSdkApi;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BidManagerFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private DependencyProvider dependencyProvider;

  @Mock
  private Publisher publisher;

  @Mock
  private TokenCache tokenCache;

  @Mock
  private User user;

  @Mock
  private SdkCache cache;

  @Mock
  private PubSdkApi api;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    when(dependencyProvider.providePubSdkApi()).thenReturn(api);
  }

  @Test
  public void prefetch_GivenAdUnits_ShouldCallCdbAndPopulateCacheWithResult() throws Exception {
    BannerAdUnit adUnit1 = new BannerAdUnit("adUnit1", new AdSize(1, 1));
    AdUnit adUnit2 = new InterstitialAdUnit("adUnit2");
    List<AdUnit> prefetchAdUnits = Arrays.asList(adUnit1, adUnit2);

    Slot slot1 = mock(Slot.class);
    Cdb response = mock(Cdb.class);
    when(response.getSlots()).thenReturn(Collections.singletonList(slot1));

    when(api.loadCdb(any(), any(), any())).thenReturn(response);

    BidManager bidManager = createBidManager();
    bidManager.prefetch(prefetchAdUnits);
    waitForIdleState();

    verify(cache).addAll(Collections.singletonList(slot1));
    verify(api).loadCdb(any(), any(), any());
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private BidManager createBidManager() {
    Context context = InstrumentationRegistry.getContext();

    return new BidManager(
        context,
        publisher,
        tokenCache,
        new DeviceInfo(),
        user,
        cache,
        new Hashtable<>(),
        dependencyProvider.provideConfig(context),
        dependencyProvider.provideAndroidUtil(context),
        dependencyProvider.provideDeviceUtil(context),
        dependencyProvider.provideLoggingUtil(),
        dependencyProvider.provideAdvertisingInfo(),
        dependencyProvider.provideClock(),
        dependencyProvider.provideUserPrivacyUtil(context)
    );
  }

}
