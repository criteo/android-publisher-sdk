package com.criteo.publisher;

import static com.criteo.publisher.ThreadingUtil.waitForAllThreads;
import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
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
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
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

  @Mock
  private Clock clock;

  private int adUnitId = 0;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    when(dependencyProvider.providePubSdkApi(any())).thenReturn(api);
    when(dependencyProvider.provideClock()).thenReturn(clock);

    // Should be set to at least 1 because user-level silent mode is set the 0 included
    givenMockedClockSetTo(1);

    // Given unrelated ad units in the cache, the tests should ignore them
    givenNotExpiredValidCachedBid(sampleAdUnit());
    givenExpiredValidCachedBid(sampleAdUnit());
    givenNotExpiredSilentModeBidCached(sampleAdUnit());
    givenExpiredSilentModeBidCached(sampleAdUnit());
    givenNoBidCached(sampleAdUnit());
    givenNoLastBid(sampleAdUnit());
  }

  @Test
  public void prefetch_GivenAdUnits_ShouldCallCdbAndPopulateCacheWithResult() throws Exception {
    List<AdUnit> prefetchAdUnits = Arrays.asList(
        mock(AdUnit.class),
        mock(AdUnit.class),
        mock(AdUnit.class));

    List<CacheAdUnit> mappedAdUnits = Arrays.asList(
        sampleAdUnit(),
        sampleAdUnit());

    AdUnitMapper mapper = givenMockedAdUnitMapper();

    Slot slot = givenMockedCdbRespondingSlot();

    when(mapper.convertValidAdUnits(prefetchAdUnits)).thenReturn(mappedAdUnits);

    BidManager bidManager = createBidManager();
    bidManager.prefetch(prefetchAdUnits);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(mappedAdUnits, slot);
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
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenNotExpiredValidCachedBid(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertEquals(slot, bid);
    verify(cache).remove(cacheAdUnit);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotExpiredValidCachedBid_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenNotExpiredValidCachedBid(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenAdUnitBeingLoaded_ShouldCallCdbAndPopulateCacheOnlyOnceForThePendingCall() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);

    Cdb response = mock(Cdb.class);
    Slot slot = mock(Slot.class);
    when(slot.isValid()).thenReturn(true);
    when(response.getSlots()).thenReturn(singletonList(slot));

    // We force a synchronization here to make the test deterministic.
    // Hence we can predict that the second bid manager call is done after the cdb call.
    // The test should also work in the other way (see the other "given ad unit being loaded" test).
    CountDownLatch cdbRequestHasStarted = new CountDownLatch(1);

    CountDownLatch cdbRequestIsPending = new CountDownLatch(1);
    when(api.loadCdb(any(), any())).thenAnswer(invocation -> {
      cdbRequestHasStarted.countDown();
      cdbRequestIsPending.await();
      return response;
    });

    BidManager bidManager = spy(createBidManager());
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    cdbRequestHasStarted.await();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    cdbRequestIsPending.countDown();
    waitForIdleState();

    // It is expected, with those two calls to the bid manager, that only one CDB call and only one
    // cache update is done. Indeed, the only CDB call correspond to the one mocked above with the
    // latch "slowing the network call". The only cache update is the one done after this single CDB
    // call. Hence, the second bid manager call, which happen between the CDB call and the cache
    // update should do nothing.

    InOrder inOrder = inOrder(bidManager, api, cache);
    inOrder.verify(bidManager).getBidForAdUnitAndPrefetch(adUnit);
    inOrder.verify(api).loadCdb(any(), any());
    inOrder.verify(bidManager).getBidForAdUnitAndPrefetch(adUnit);
    inOrder.verify(cache).add(slot);
    inOrder.verify(bidManager).setTimeToNextCall(anyInt());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenAdUnitBeingLoaded_ShouldCallCdbAndPopulateCacheOnlyOnceForThePendingCall2() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    // We force the CDB call to be after the second bid manager call to make the test deterministic.
    CountDownLatch bidManagerIsCalledASecondTime = givenExecutorWaitingOn();

    BidManager bidManager = spy(createBidManager());
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    bidManagerIsCalledASecondTime.countDown();

    // It is expected, with those two calls to the bid manager, that only one CDB call and only one
    // cache update is done. Indeed, the only CDB call correspond to the one triggered by the first
    // bid manager call but run after the second bid manager call. The only cache update is the one
    // done after this single CDB call. Hence, the second bid manager call, which happen before the
    // CDB call and the cache update should do nothing.

    InOrder inOrder = inOrder(bidManager, api, cache);
    inOrder.verify(bidManager).getBidForAdUnitAndPrefetch(adUnit);
    inOrder.verify(bidManager).getBidForAdUnitAndPrefetch(adUnit);
    inOrder.verify(api, timeout(1000)).loadCdb(any(), any());
    inOrder.verify(cache).add(slot);
    inOrder.verify(bidManager).setTimeToNextCall(anyInt());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenEmptyCache_ReturnNull() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);

    givenNoLastBid(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertNull(bid);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenEmptyCache_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    givenNoLastBid(cacheAdUnit);

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenClockAtFixedTime_CacheShouldContainATimestampedBid() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();
    givenMockedClockSetTo(42);

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    verify(cache).add(slot);
    verify(slot).setTimeOfDownload(42);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenExpiredValidCachedBid_ReturnNull() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenExpiredValidCachedBid(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertNull(bid);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenExpiredValidCachedBid_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenExpiredValidCachedBid(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNoBidCached_ReturnNull() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenNoBidCached(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertNull(bid);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNoBidCached_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenNoBidCached(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotExpiredSilentModeBidCached_ReturnNullAndDoNotRemoveIt() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenNotExpiredSilentModeBidCached(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertNull(bid);
    verify(cache, never()).remove(cacheAdUnit);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotExpiredSilentModeBidCached_ShouldNotCallCdbAndNotPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenNotExpiredSilentModeBidCached(cacheAdUnit);

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertShouldNotCallCdbAndNotPopulateCache();
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenExpiredSilentModeBidCached_ReturnNull() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenExpiredSilentModeBidCached(cacheAdUnit);

    BidManager bidManager = createBidManager();
    Slot bid = bidManager.getBidForAdUnitAndPrefetch(adUnit);

    assertNull(bid);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenExpiredSilentModeBidCached_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    givenExpiredSilentModeBidCached(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenNotExpiredUserLevelSilentMode_ShouldNotCallCdbAndNotPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);

    BidManager bidManager = createBidManager();
    givenMockedClockSetTo(0);
    bidManager.setTimeToNextCall(60); // Silent until 60_000 included
    givenMockedClockSetTo(60_000);
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldNotCallCdbAndNotPopulateCache();
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenExpiredUserLevelSilentMode_ShouldCallCdbAndPopulateCache() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    BidManager bidManager = createBidManager();
    givenMockedClockSetTo(0);
    bidManager.setTimeToNextCall(60); // Silent until 60_000 included
    givenMockedClockSetTo(60_001);
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenCdbCallAndCachedPopulatedWithUserLevelSilentMode_UserLevelSilentModeIsUpdated() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Cdb cdb = givenMockedCdbResponse();

    when(cdb.getTimeToNextCall()).thenReturn(1337);

    BidManager bidManager = spy(createBidManager());
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    verify(bidManager).setTimeToNextCall(1337);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenFirstCdbCallWithoutUserLevelSilenceAndASecondFetchJustAfter_SecondFetchIsNotSilenced() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);

    BidManager bidManager = createBidManager();

    // Given first CDB call without user-level silence
    Cdb cdb = givenMockedCdbResponse();
    when(cdb.getTimeToNextCall()).thenReturn(0);
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    // Count calls from this point
    clearInvocations(cache);
    clearInvocations(api);

    // Given a second fetch, without any clock change
    Slot slot = givenMockedCdbRespondingSlot();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    assertShouldCallCdbAndPopulateCacheOnlyOnce(singletonList(cacheAdUnit), slot);
  }

  @Test
  public void getBidForAdUnitAndPrefetch_GivenCdbGivingInvalidSlots_IgnoreThem() throws Exception {
    CacheAdUnit cacheAdUnit = sampleAdUnit();
    AdUnit adUnit = givenMockedAdUnitMappingTo(cacheAdUnit);
    Slot slot = givenMockedCdbRespondingSlot();

    when(slot.isValid()).thenReturn(false);

    BidManager bidManager = createBidManager();
    bidManager.getBidForAdUnitAndPrefetch(adUnit);
    waitForIdleState();

    verify(cache, never()).add(slot);
  }

  private void assertShouldCallCdbAndPopulateCacheOnlyOnce(List<CacheAdUnit> requestedAdUnits, Slot slot) {
    verify(cache).add(slot);
    verify(api).loadCdb(argThat(cdb -> {
      assertEquals(requestedAdUnits, cdb.getRequestedAdUnits());
      return true;
    }), any());
  }

  private void assertShouldNotCallCdbAndNotPopulateCache() {
    verify(cache, never()).add(any());
    verify(api, never()).loadCdb(any(), any());
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  @NonNull
  private Slot givenNotExpiredValidCachedBid(CacheAdUnit cacheAdUnit) {
    // not expired := tod + ttl * 1000 > clock
    // not expired := tod > clock - ttl * 1000
    // not expired := tod > clock - 60000
    long timeOfDownload = clock.getCurrentTimeInMillis() - 60_000 + 1;
    Slot slot = mock(Slot.class);
    when(slot.getCpmAsNumber()).thenReturn(1.);
    when(slot.getTimeOfDownload()).thenReturn(timeOfDownload);
    when(slot.getTtl()).thenReturn(60);

    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
    return slot;
  }

  private void givenExpiredValidCachedBid(CacheAdUnit cacheAdUnit) {
    long timeOfDownload = clock.getCurrentTimeInMillis() - 60_000;
    Slot slot = mock(Slot.class);
    when(slot.getCpmAsNumber()).thenReturn(1.);
    when(slot.getTimeOfDownload()).thenReturn(timeOfDownload);
    when(slot.getTtl()).thenReturn(60);

    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
  }

  private void givenNoBidCached(CacheAdUnit cacheAdUnit) {
    Slot slot = mock(Slot.class);
    when(slot.getCpmAsNumber()).thenReturn(0.);
    when(slot.getTtl()).thenReturn(0);

    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
  }

  private void givenNoLastBid(CacheAdUnit cacheAdUnit) {
    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(null);
  }

  private void givenNotExpiredSilentModeBidCached(CacheAdUnit cacheAdUnit) {
    long timeOfDownload = clock.getCurrentTimeInMillis() - 60_000 + 1;

    Slot slot = mock(Slot.class);
    when(slot.getCpmAsNumber()).thenReturn(0.);
    when(slot.getTimeOfDownload()).thenReturn(timeOfDownload);
    when(slot.getTtl()).thenReturn(60);

    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
  }

  private void givenExpiredSilentModeBidCached(CacheAdUnit cacheAdUnit) {
    long timeOfDownload = clock.getCurrentTimeInMillis() - 60_000;

    Slot slot = mock(Slot.class);
    when(slot.getCpmAsNumber()).thenReturn(0.);
    when(slot.getTimeOfDownload()).thenReturn(timeOfDownload);
    when(slot.getTtl()).thenReturn(60);

    when(cache.peekAdUnit(cacheAdUnit)).thenReturn(slot);
  }

  @NonNull
  private CacheAdUnit sampleAdUnit() {
    return new CacheAdUnit(new AdSize(1, 1), "adUnit" + adUnitId++, CRITEO_BANNER);
  }

  @NonNull
  private CountDownLatch givenExecutorWaitingOn() {
    CountDownLatch waitingLatch = new CountDownLatch(1);

    when(dependencyProvider.provideThreadPoolExecutor()).thenAnswer(invocation -> {
      Executor executor = (Executor) invocation.callRealMethod();
      return (Executor) command -> {
        Runnable waitingCommand = () -> {
          try {
            waitingLatch.await();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          command.run();
        };

        executor.execute(waitingCommand);
      };
    });

    return waitingLatch;
  }

  private Slot givenMockedCdbRespondingSlot() {
    Slot slot = mock(Slot.class);
    when(slot.isValid()).thenReturn(true);
    Cdb response = givenMockedCdbResponse();
    when(response.getSlots()).thenReturn(singletonList(slot));
    return slot;
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
    when(clock.getCurrentTimeInMillis()).thenReturn(instant);
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
