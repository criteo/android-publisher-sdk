package com.criteo.publisher;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import com.criteo.publisher.Util.AppLifecycleUtil;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.DirectMockRunOnUiThreadExecutor;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.network.PubSdkApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CriteoInternalUnitTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Application application;

  private List<AdUnit> adUnits;

  private String criteoPublisherId = "B-000001";

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
  public void whenCreatingNewCriteo_GivenNullCpId_ThrowExceptionAndDoNotUseNetwork() throws Exception {
    PubSdkApi api = givenMockedPubSdkApi();
    criteoPublisherId = null;

    assertThatThrownBy(this::createCriteo).isInstanceOf(IllegalArgumentException.class);
    verifyZeroInteractions(api);
  }

  @Test
  public void whenCreatingNewCriteo_GivenBidManagerAndAdUnits_ShouldCallPrefetchWithAdUnits() throws Exception {
    BidManager bidManager = givenMockedBidManager();
    adUnits = mock(List.class);

    createCriteo();

    verify(bidManager).prefetch(adUnits);
  }

  @Test
  public void whenCreatingNewCriteo_GivenBidManagerAndNullAdUnits_ShouldCallPrefetchWithEmptyAdUnits() throws Exception {
    BidManager bidManager = givenMockedBidManager();
    adUnits = null;

    createCriteo();

    verify(bidManager).prefetch(Collections.emptyList());
  }

  @Test
  public void whenCreatingNewCriteo_GivenApplication_ShouldCreateSupportedScreenSizes() throws Exception {
    DeviceUtil deviceUtil = mock(DeviceUtil.class);

    Context context = application.getApplicationContext();
    when(dependencyProvider.provideDeviceUtil(context)).thenReturn(deviceUtil);

    createCriteo();

    verify(deviceUtil).createSupportedScreenSizes(application);
  }

  @Test
  public void whenCreatingNewCriteo_GivenDependencyProvider_OnlyUseApplicationContextToBuildDependencies()
      throws Exception {
    // Used to verify transitive dependencies
    dependencyProvider = spy(DependencyProvider.getInstance());
    doReturn(mock(DeviceUtil.class)).when(dependencyProvider).provideDeviceUtil(any());
    doReturn(mock(UserPrivacyUtil.class)).when(dependencyProvider).provideUserPrivacyUtil(any());
    doReturn(mock(Config.class)).when(dependencyProvider).provideConfig(any());
    doReturn(new DirectMockRunOnUiThreadExecutor()).when(dependencyProvider).provideRunOnUiThreadExecutor();

    Context applicationContext = mock(Context.class, Answers.RETURNS_DEEP_STUBS);
    when(application.getApplicationContext()).thenReturn(applicationContext);
    criteoPublisherId = "B-123456";

    createCriteo();

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(dependencyProvider, atLeastOnce()).provideDeviceUtil(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideDeviceInfo(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideAdUnitMapper(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideAndroidUtil(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideBidManager(contextCaptor.capture(), eq(criteoPublisherId));
    verify(dependencyProvider, atLeastOnce()).provideConfig(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideUserPrivacyUtil(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideUser(contextCaptor.capture());
    verify(dependencyProvider, atLeastOnce()).provideAppEvents(contextCaptor.capture());

    assertThat(contextCaptor.getAllValues()).containsOnly(applicationContext);
  }

  @Test
  public void whenCreatingNewCriteo_GivenTrueUsOptOut_ShouldStoreTrueValue() throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = true;

    createCriteo();

    verify(userPrivacyUtil).storeUsPrivacyOptout(true);
  }

  @Test
  public void whenCreatingNewCriteo_GivenTrueUsOptOut_ThenSetToFalse_ShouldStoreTrueThenFalseValue() throws Exception {
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
  public void whenCreatingNewCriteo_GivenFalseUsOptOut_ThenSetToTrue_ShouldFalseThenTrue() throws Exception {
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
  public void whenCreatingNewCriteo_GivenNullUsOptOut_ThenSetToTrue_ShouldStoreTrueValue() throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = null;

    CriteoInternal criteoInternal = createCriteo();
    criteoInternal.setUsPrivacyOptOut(true);

    verify(userPrivacyUtil).storeUsPrivacyOptout(true);
  }

  @Test
  public void whenCreatingNewCriteo_GivenNullUsOptOut_ThenSetToFalse_ShouldStoreFalseValue() throws Exception {
    givenMockedUserPrivacyUtil();
    usPrivacyOptout = null;

    CriteoInternal criteoInternal = createCriteo();
    criteoInternal.setUsPrivacyOptOut(false);

    verify(userPrivacyUtil).storeUsPrivacyOptout(false);
  }

  @Test
  public void whenCreatingNewCriteo_GivenDeviceInfo_InitializeIt() throws Exception {
    DeviceInfo deviceInfo = mock(DeviceInfo.class);
    doReturn(deviceInfo).when(dependencyProvider).provideDeviceInfo(any());

    createCriteo();

    verify(deviceInfo).initialize();
  }

  @Test
  public void whenCreatingNewCriteo_GivenApplication_RegisterOneActivityLifecycleCallback() throws Exception {
    createCriteo();

    verify(application).registerActivityLifecycleCallbacks(any(AppLifecycleUtil.class));
  }

  @Test
  public void getBidResponse_GivenBidManagerThrowing_DoNotThrowAndReturnNoBidResponse() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);

    BidManager bidManager = givenMockedBidManager();
    when(bidManager.getBidForInhouseMediation(adUnit)).thenThrow(RuntimeException.class);

    BidResponse noBid = new BidResponse(0., null, false);

    Criteo criteo = createCriteo();
    BidResponse bidResponse = criteo.getBidResponse(adUnit);

    assertThat(bidResponse).isEqualTo(noBid);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldingOne_ReturnIt() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    BidResponse expectedBid = new BidResponse(42., new BidToken(UUID.randomUUID(), adUnit), true);

    BidManager bidManager = givenMockedBidManager();
    when(bidManager.getBidForInhouseMediation(adUnit)).thenReturn(expectedBid);

    Criteo criteo = createCriteo();
    BidResponse bidResponse = criteo.getBidResponse(adUnit);

    assertThat(bidResponse).isEqualTo(expectedBid);
  }

  @Test
  public void whenCreatingNewCriteo_GivenNonNullMopubConsent_ShouldCallStoreMethod() throws Exception {
    Context context = application.getApplicationContext();
    when(dependencyProvider.provideUserPrivacyUtil(context)).thenReturn(userPrivacyUtil);
    mopubConsentValue = "fake_mopub_consent_value";

    createCriteo();

    verify(userPrivacyUtil).storeMopubConsent("fake_mopub_consent_value");
  }

  @Test
  public void whenCreatingNewCriteo_GivenNullMopubConsent_ShouldNotCallStoreMethod() throws Exception {
    Context context = application.getApplicationContext();
    when(dependencyProvider.provideUserPrivacyUtil(context)).thenReturn(userPrivacyUtil);
    mopubConsentValue = null;

    createCriteo();

    verify(userPrivacyUtil, never()).storeMopubConsent("fake_mopub_consent_value");
  }

  @Test
  public void setBidsForAdUnit_GivenBidManagerThrowing_DoNotThrow() throws Exception {
    BidManager bidManager = givenMockedBidManager();
    doThrow(RuntimeException.class).when(bidManager).enrichBid(any(), any());

    CriteoInternal criteo = createCriteo();

    assertThatCode(() -> {
      criteo.setBidsForAdUnit(mock(Object.class), mock(AdUnit.class));
    }).doesNotThrowAnyException();
  }

  @Test
  public void setBidsForAdUnit_GivenBidManager_DelegateToIt() throws Exception {
    BidManager bidManager = givenMockedBidManager();

    Object object = mock(Object.class);
    AdUnit adUnit = mock(AdUnit.class);

    CriteoInternal criteo = createCriteo();
    criteo.setBidsForAdUnit(object, adUnit);

    verify(bidManager).enrichBid(object, adUnit);
  }

  @Test
  public void getBidForAdUnit_GivenBidManager_DelegateToIt() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    Slot expected = mock(Slot.class);

    BidManager bidManager = givenMockedBidManager();
    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(expected);

    CriteoInternal criteo = createCriteo();
    Slot bid = criteo.getBidForAdUnit(adUnit);

    assertThat(bid).isSameAs(expected);
  }

  @Test
  public void getTokenValue_GivenBidManager_DelegateToIt() throws Exception {
    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));
    TokenValue expected = mock(TokenValue.class);

    BidManager bidManager = givenMockedBidManager();
    when(bidManager.getTokenValue(token, CRITEO_BANNER)).thenReturn(expected);

    CriteoInternal criteo = createCriteo();
    TokenValue tokenValue = criteo.getTokenValue(token, CRITEO_BANNER);

    assertThat(tokenValue).isSameAs(expected);
  }

  private void givenMockedUserPrivacyUtil() {
    Context context = application.getApplicationContext();
    when(dependencyProvider.provideUserPrivacyUtil(context)).thenReturn(userPrivacyUtil);
  }

  private BidManager givenMockedBidManager() {
    BidManager bidManager = mock(BidManager.class);

    Context context = application.getApplicationContext();
    when(dependencyProvider.provideBidManager(context, criteoPublisherId)).thenReturn(bidManager);

    return bidManager;
  }

  private PubSdkApi givenMockedPubSdkApi() {
    PubSdkApi api = mock(PubSdkApi.class);

    Context context = application.getApplicationContext();
    when(dependencyProvider.providePubSdkApi(context)).thenReturn(api);

    return api;
  }

  private CriteoInternal createCriteo() {
    return new CriteoInternal(application, adUnits, criteoPublisherId, usPrivacyOptout, mopubConsentValue, dependencyProvider);
  }

}
