package com.criteo.publisher;

import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Util.AndroidUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.DeviceInfo;
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

    when(dependencyProvider.providePubSdkApi(any())).thenReturn(api);
  }

  @Test
  public void prefetch_GivenAdUnits_ShouldCallCdbAndPopulateCacheWithResult() throws Exception {
    List<AdUnit> prefetchAdUnits = Arrays.asList(
        mock(AdUnit.class),
        mock(AdUnit.class),
        mock(AdUnit.class));

    List<CacheAdUnit> mappedAdUnits = Arrays.asList(
        sampleAdUnit(1),
        sampleAdUnit(2));

    AdUnitMapper mapper = givenMockedAdUnitMapper();

    List<Slot> slots = givenMockedCdbRespondingSlots();

    when(mapper.convertValidAdUnits(prefetchAdUnits)).thenReturn(mappedAdUnits);

    BidManager bidManager = createBidManager();
    bidManager.prefetch(prefetchAdUnits);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(mappedAdUnits, slots);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotValidAdUnit_ReturnNullAndDoNotCallCdb() {
    AdUnit adUnit = givenMockedAdUnitMappingTo(null);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertNull(bid);
    verify(api, never()).loadCdb(any(), any());
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNullAdUnit_ReturnNullAndDoNotCallCdb() {
    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(null);
    waitForIdleState();

    assertNull(bid);
    verify(api, never()).loadCdb(any(), any());
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotExpiredValidCachedBid_ReturnItAndRemoveItFromCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit(1);
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenNotExpiredValidCachedBid(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertEquals(slot, bid);
    verify(cache).remove(cacheAdUnit);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotExpiredValidCachedBid_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit(1);
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenNotExpiredValidCachedBid(cacheAdUnit);
    List<Slot> slots = givenMockedCdbRespondingSlots();

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(Collections.singletonList(cacheAdUnit), slots);
  }

  private void assertShouldCallCdbAndPopulateCacheOnlyOnce(List<CacheAdUnit> requestedAdUnits, List<Slot> slots) {
    verify(cache).addAll(slots);
    verify(api).loadCdb(argThat(cdb -> {
      assertEquals(requestedAdUnits, cdb.getRequestedAdUnits());
      return true;
    }), any());
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  @NonNull
  private Slot givenNotExpiredValidCachedBid(CacheAdUnit cacheAdUnit) {
    Slot slot = mock(Slot.class);
    when(slot.getCpmAsNumber()).thenReturn(1.);
    when(slot.getTimeOfDownload()).thenReturn(100_000L);
    when(slot.getTtl()).thenReturn(60); // Valid until 160_000 included

    givenMockedClockSetTo(160_000);

    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
    return slot;
  }

  @NonNull
  private CacheAdUnit sampleAdUnit(int id) {
    return new CacheAdUnit(new AdSize(1, 1), "adUnit" + id, CRITEO_BANNER);
  }

  private List<Slot> givenMockedCdbRespondingSlots() {
    List<Slot> slots = Collections.singletonList(mock(Slot.class));
    Cdb response = givenMockedCdbResponse();
    when(response.getSlots()).thenReturn(slots);
    return slots;
  }

  private Cdb givenMockedCdbResponse() {
    Cdb response = mock(Cdb.class);
    when(api.loadCdb(any(), any())).thenReturn(response);
    return response;
  }

  private AdUnitMapper givenMockedAdUnitMapper() {
    AdUnitMapper mapper = mock(AdUnitMapper.class);
    when(dependencyProvider.provideAdUnitMapper(any(), any())).thenReturn(mapper);
    return mapper;
  }

  private AdUnit givenMockedAdUnitMappingTo(CacheAdUnit toAdUnit) {
    AdUnit fromAdUnit = mock(AdUnit.class);

    AdUnitMapper adUnitMapper = givenMockedAdUnitMapper();
    when(adUnitMapper.convertValidAdUnit(fromAdUnit)).thenReturn(toAdUnit);

    return fromAdUnit;
  }

  private void givenMockedClockSetTo(long instant) {
    Clock clock = mock(Clock.class);
    when(clock.getCurrentTimeInMillis()).thenReturn(instant);

    when(dependencyProvider.provideClock()).thenReturn(clock);
  }

  private BidManager createBidManager() {
    Context context = InstrumentationRegistry.getContext();
    AndroidUtil androidUtil = dependencyProvider.provideAndroidUtil(context);
    DeviceUtil deviceUtil = dependencyProvider.provideDeviceUtil(context);

    return new BidManager(
        publisher,
        tokenCache,
        new DeviceInfo(),
        user,
        cache,
        new Hashtable<>(),
        dependencyProvider.provideConfig(context),
        deviceUtil,
        dependencyProvider.provideLoggingUtil(),
        dependencyProvider.provideClock(),
        dependencyProvider.provideUserPrivacyUtil(context),
        dependencyProvider.provideAdUnitMapper(androidUtil, deviceUtil),
        dependencyProvider.providePubSdkApi(context)
    );
  }

}
