package com.criteo.publisher;

import static com.criteo.publisher.CriteoUtil.clearCriteo;
import static com.criteo.publisher.CriteoUtil.getCriteoBuilder;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.network.PubSdkApi;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserPrivacyFunctionalTest {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private PubSdkApi pubSdkApi;

  @Inject
  private Context context;

  private SharedPreferences defaultSharedPreferences;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    doReturn(pubSdkApi).when(dependencyProvider).providePubSdkApi();
    mockedDependenciesRule.givenMockedRemoteConfigResponse(pubSdkApi);
  }

  @After
  public void after() {
    defaultSharedPreferences.edit().clear().commit();
  }

  @Test
  public void whenCriteoInit_GivenUspIabNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", "fake_iab_usp");

    givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_iab_usp", cdb.getUser().uspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspIabEmpty_VerifyItIsNotPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", null);

    givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    Assert.assertNull(cdb.getUser().uspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.usPrivacyOptOut(true).init();

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("true", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.init();

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertNull(cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalse_VerifyFalseIsPassedToCdb()
      throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();
    criteo.setUsPrivacyOptOut(false);

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalseAfterFirstCall_VerifyFalseIsPassedToCdbOnTheSecondCall()
      throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();

    waitForIdleState();

    criteo.setUsPrivacyOptOut(false);
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480);

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.mopubConsent("fake_mopub_consent").init();

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentNotProvided_ThenProvidedAfterFirstCall_VerifyItIsPassedToCdbOnTheSecondCall()
      throws Exception {
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.init();

    waitForIdleState();

    criteo.setMopubConsent("fake_mopub_consent");
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480);

    waitForIdleState();

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentThroughSetter_ThenCriteoCleared_ThenVerifyItIsStillPassedToCdb()
      throws Exception {
    // given
    Criteo.Builder builder = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.init();

    waitForIdleState();

    criteo.setMopubConsent("fake_mopub_consent");

    // when
    clearCriteo();

    // then
    Criteo.Builder builder2 = getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo2 = builder2.init();
    criteo2.getBidForAdUnit(TestAdUnits.BANNER_320_480);
    waitForIdleState();
    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(3)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  private void writeIntoDefaultSharedPrefs(String key, String value) {
    Editor edit = defaultSharedPreferences.edit();
    edit.putString(key, value);
    edit.commit();
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }
}
