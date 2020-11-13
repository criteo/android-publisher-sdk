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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.headerbidding.HeaderBidding;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AppLifecycleUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInternalUnitTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Application application;

  private List<AdUnit> adUnits;

  private Boolean usPrivacyOptout = false;

  private String mopubConsentValue;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DependencyProvider dependencyProvider;

  @Mock
  private UserPrivacyUtil userPrivacyUtil;

  @Mock
  private Config config;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(dependencyProvider.provideRunOnUiThreadExecutor())
        .thenReturn(new DirectMockRunOnUiThreadExecutor());

    when(dependencyProvider.provideConfig()).thenReturn(config);

    adUnits = new ArrayList<>();
  }

  @Test
  public void whenCreatingNewCriteo_GivenBidManagerAndBidLifecycleListener_ShouldCallListenerBeforePrefetch()
      throws Exception {
    BidLifecycleListener listener = givenMockedBidLifecycleListener();
    BidManager bidManager = givenMockedBidManager();

    createCriteo();

    InOrder inOrder = inOrder(listener, bidManager);
    inOrder.verify(listener).onSdkInitialized();
    inOrder.verify(bidManager).prefetch(any());
  }

  @Test
  public void whenCreatingNewCriteo_GivenBidManagerAndAdUnits_ShouldCallPrefetchWithAdUnits()
      throws Exception {
    BidManager bidManager = givenMockedBidManager();
    adUnits = mock(List.class);

    createCriteo();

    verify(bidManager).prefetch(adUnits);
  }

  @Test
  public void whenCreatingNewCriteo_GivenTrueUsOptOut_ShouldStoreTrueValue() throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = true;

    createCriteo();

    verify(userPrivacyUtil).storeUsPrivacyOptout(true);
  }

  @Test
  public void whenCreatingNewCriteo_GivenTrueUsOptOut_ThenSetToFalse_ShouldStoreTrueThenFalseValue()
      throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = true;

    CriteoInternal criteoInternal = createCriteo();
    verify(userPrivacyUtil).storeUsPrivacyOptout(true);

    criteoInternal.setUsPrivacyOptOut(false);
    verify(userPrivacyUtil).storeUsPrivacyOptout(false);
  }

  @Test
  public void whenCreatingNewCriteo_GivenFalseUsOptOut_ShouldStoreFalseValue() throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = false;

    createCriteo();

    verify(userPrivacyUtil).storeUsPrivacyOptout(false);
  }

  @Test
  public void whenCreatingNewCriteo_GivenFalseUsOptOut_ThenSetToTrue_ShouldFalseThenTrue()
      throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = false;

    CriteoInternal criteoInternal = createCriteo();
    verify(userPrivacyUtil).storeUsPrivacyOptout(false);

    criteoInternal.setUsPrivacyOptOut(true);
    verify(userPrivacyUtil).storeUsPrivacyOptout(true);
  }

  @Test
  public void whenCreatingNewCriteo_GivenNullUsOptOut_ShouldNotStoreIt() throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = null;

    createCriteo();

    verify(userPrivacyUtil, never()).storeUsPrivacyOptout(any(Boolean.class));
  }

  @Test
  public void whenCreatingNewCriteo_GivenNullUsOptOut_ThenSetToTrue_ShouldStoreTrueValue()
      throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = null;

    CriteoInternal criteoInternal = createCriteo();
    criteoInternal.setUsPrivacyOptOut(true);

    verify(userPrivacyUtil).storeUsPrivacyOptout(true);
  }

  @Test
  public void whenCreatingNewCriteo_GivenNullUsOptOut_ThenSetToFalse_ShouldStoreFalseValue()
      throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = null;

    CriteoInternal criteoInternal = createCriteo();
    criteoInternal.setUsPrivacyOptOut(false);

    verify(userPrivacyUtil).storeUsPrivacyOptout(false);
  }

  @Test
  public void whenCreatingNewCriteo_GivenDeviceInfo_InitializeIt() throws Exception {
    DeviceInfo deviceInfo = mock(DeviceInfo.class);
    doReturn(deviceInfo).when(dependencyProvider).provideDeviceInfo();

    createCriteo();

    verify(deviceInfo).initialize();
  }

  @Test
  public void whenCreatingNewCriteo_GivenApplication_RegisterAppLifecycleUtil()
      throws Exception {
    createCriteo();

    verify(application).registerActivityLifecycleCallbacks(any(AppLifecycleUtil.class));
  }

  @Test
  public void whenCreatingNewCriteo_GivenApplication_RegisterLastActivityTracker()
      throws Exception {
    createCriteo();

    TopActivityFinder topActivityFinder = dependencyProvider.provideTopActivityFinder();
    verify(topActivityFinder).registerActivityLifecycleFor(application);
  }

  @Test
  public void loadBid_GivenNoContext_UseEmptyContext() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    BidResponseListener listener = mock(BidResponseListener.class);

    Criteo criteo = spy(createCriteo());
    criteo.loadBid(adUnit, listener);

    verify(criteo).loadBid(eq(adUnit), eq(new ContextData()), eq(listener));
  }

  @Test
  public void loadBid_GivenBidLoaderThrowing_DoNotThrowAndReturnNoBidResponse()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    BidResponseListener listener = mock(BidResponseListener.class);
    ContextData contextData = mock(ContextData.class);

    ConsumableBidLoader consumableBidLoader = givenMockedConsumableBidLoader();
    doAnswer(invocation -> {
      throw new RuntimeException();
    }).when(consumableBidLoader).loadBid(adUnit, contextData, listener);

    Criteo criteo = createCriteo();
    criteo.loadBid(adUnit, contextData, listener);

    verify(listener).onResponse(null);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingOne_ReturnIt() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    BidResponseListener listener = mock(BidResponseListener.class);
    ContextData contextData = mock(ContextData.class);
    Bid expectedBid = mock(Bid.class);

    ConsumableBidLoader consumableBidLoader = givenMockedConsumableBidLoader();
    doAnswer(invocation -> {
      invocation.<BidResponseListener>getArgument(2).onResponse(expectedBid);
      return null;
    }).when(consumableBidLoader).loadBid(adUnit, contextData, listener);

    Criteo criteo = createCriteo();
    criteo.loadBid(adUnit, contextData, listener);

    verify(listener).onResponse(expectedBid);
  }

  @Test
  public void whenCreatingNewCriteo_GivenNonNullMopubConsent_ShouldCallStoreMethod()
      throws Exception {
    when(dependencyProvider.provideUserPrivacyUtil()).thenReturn(userPrivacyUtil);
    mopubConsentValue = "fake_mopub_consent_value";

    createCriteo();

    verify(userPrivacyUtil).storeMopubConsent("fake_mopub_consent_value");
  }

  @Test
  public void whenCreatingNewCriteo_GivenNullMopubConsent_ShouldNotCallStoreMethod()
      throws Exception {
    when(dependencyProvider.provideUserPrivacyUtil()).thenReturn(userPrivacyUtil);
    mopubConsentValue = null;

    createCriteo();

    verify(userPrivacyUtil, never()).storeMopubConsent("fake_mopub_consent_value");
  }

  @Test
  public void setBidsForAdUnit_GivenHeaderBiddingThrowing_DoNotThrow() throws Exception {
    HeaderBidding headerBidding = givenMockedHeaderBidding();
    doThrow(RuntimeException.class).when(headerBidding).enrichBid(any(), any());

    CriteoInternal criteo = createCriteo();

    assertThatCode(() -> {
      criteo.enrichAdObjectWithBid(mock(Object.class), mock(Bid.class));
    }).doesNotThrowAnyException();
  }

  @Test
  public void setBidsForAdUnit_GivenHeaderBidding_DelegateToIt() throws Exception {
    HeaderBidding headerBidding = givenMockedHeaderBidding();

    Object object = mock(Object.class);
    Bid bid = mock(Bid.class);

    CriteoInternal criteo = createCriteo();
    criteo.enrichAdObjectWithBid(object, bid);

    verify(headerBidding).enrichBid(object, bid);
  }

  @Test
  public void getBidForAdUnit_GivenBidManager_DelegateToIt() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    ContextData contextData = mock(ContextData.class);
    BidListener bidListener = mock(BidListener.class);

    BidManager bidManager = givenMockedBidManager();

    CriteoInternal criteo = createCriteo();
    criteo.getBidForAdUnit(adUnit, contextData, bidListener);

    verify(bidManager).getBidForAdUnit(adUnit, contextData, bidListener);
  }

  private void givenMockedUserPrivacyUtil() {
    when(dependencyProvider.provideUserPrivacyUtil()).thenReturn(userPrivacyUtil);
  }

  private BidLifecycleListener givenMockedBidLifecycleListener() {
    BidLifecycleListener listener = mock(BidLifecycleListener.class);

    when(dependencyProvider.provideBidLifecycleListener()).thenReturn(listener);

    return listener;
  }

  private BidManager givenMockedBidManager() {
    BidManager bidManager = mock(BidManager.class);

    when(dependencyProvider.provideBidManager()).thenReturn(bidManager);

    return bidManager;
  }

  private HeaderBidding givenMockedHeaderBidding() {
    HeaderBidding headerBidding = mock(HeaderBidding.class);

    when(dependencyProvider.provideHeaderBidding()).thenReturn(headerBidding);

    return headerBidding;
  }

  private ConsumableBidLoader givenMockedConsumableBidLoader() {
    ConsumableBidLoader consumableBidLoader = mock(ConsumableBidLoader.class);

    when(dependencyProvider.provideConsumableBidLoader()).thenReturn(consumableBidLoader);

    return consumableBidLoader;
  }

  private CriteoInternal createCriteo() {
    return new CriteoInternal(application, adUnits, usPrivacyOptout,
        mopubConsentValue, dependencyProvider
    );
  }
}
