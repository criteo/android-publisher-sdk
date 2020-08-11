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

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor;
import com.criteo.publisher.headerbidding.HeaderBidding;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.DisplayUrlTokenValue;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AppLifecycleUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(dependencyProvider.provideRunOnUiThreadExecutor())
        .thenReturn(new DirectMockRunOnUiThreadExecutor());

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
  public void whenCreatingNewCriteo_GivenBidManagerAndNullAdUnits_ShouldCallPrefetchWithEmptyAdUnits()
      throws Exception {
    BidManager bidManager = givenMockedBidManager();
    adUnits = null;

    createCriteo();

    verify(bidManager).prefetch(Collections.emptyList());
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
  public void getBidResponse_GivenInHouseThrowing_DoNotThrowAndReturnNoBidResponse()
      throws Exception {
    AdUnit adUnit = mock(AdUnit.class);

    InHouse inHouse = givenMockedInHouse();
    when(inHouse.getBidResponse(adUnit)).thenThrow(RuntimeException.class);

    BidResponse noBid = new BidResponse(0., null, false);

    Criteo criteo = createCriteo();
    BidResponse bidResponse = criteo.getBidResponse(adUnit);

    assertThat(bidResponse).isEqualTo(noBid);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingOne_ReturnIt() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    BidResponse expectedBid = new BidResponse(42., new BidToken(UUID.randomUUID(), adUnit), true);

    InHouse inHouse = givenMockedInHouse();
    when(inHouse.getBidResponse(adUnit)).thenReturn(expectedBid);

    Criteo criteo = createCriteo();
    BidResponse bidResponse = criteo.getBidResponse(adUnit);

    assertThat(bidResponse).isEqualTo(expectedBid);
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
      criteo.setBidsForAdUnit(mock(Object.class), mock(AdUnit.class));
    }).doesNotThrowAnyException();
  }

  @Test
  public void setBidsForAdUnit_GivenHeaderBidding_DelegateToIt() throws Exception {
    HeaderBidding headerBidding = givenMockedHeaderBidding();

    Object object = mock(Object.class);
    AdUnit adUnit = mock(AdUnit.class);

    CriteoInternal criteo = createCriteo();
    criteo.setBidsForAdUnit(object, adUnit);

    verify(headerBidding).enrichBid(object, adUnit);
  }

  @Test
  public void getBidForAdUnit_GivenBidManager_DelegateToIt() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    CdbResponseSlot expected = mock(CdbResponseSlot.class);

    BidManager bidManager = givenMockedBidManager();
    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(expected);

    CriteoInternal criteo = createCriteo();
    CdbResponseSlot bid = criteo.getBidForAdUnit(adUnit);

    assertThat(bid).isSameAs(expected);
  }

  @Test
  public void getTokenValue_GivenInHouse_DelegateToIt() throws Exception {
    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    DisplayUrlTokenValue expected = mock(DisplayUrlTokenValue.class);

    InHouse inHouse = givenMockedInHouse();
    when(inHouse.getTokenValue(token, CRITEO_BANNER)).thenReturn(expected);

    CriteoInternal criteo = createCriteo();
    DisplayUrlTokenValue tokenValue = criteo.getTokenValue(token, CRITEO_BANNER);

    assertThat(tokenValue).isSameAs(expected);
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

  private InHouse givenMockedInHouse() {
    InHouse inHouse = mock(InHouse.class);

    when(dependencyProvider.provideInHouse()).thenReturn(inHouse);

    return inHouse;
  }

  private CriteoInternal createCriteo() {
    return new CriteoInternal(application, adUnits, usPrivacyOptout,
        mopubConsentValue, dependencyProvider);
  }

}
