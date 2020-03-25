package com.criteo.publisher;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.network.PubSdkApi;
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

  private SharedPreferences defaultSharedPreferences;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();
    defaultSharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(app.getApplicationContext());

    doReturn(pubSdkApi).when(dependencyProvider).providePubSdkApi();
  }

  @After
  public void after() {
    defaultSharedPreferences.edit().clear().commit();
  }

  @Test
  public void whenCriteoInit_GivenUspIabNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", "fake_iab_usp");

    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_iab_usp", cdb.getUser().uspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspIabEmpty_VerifyItIsNotPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", null);

    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    Assert.assertNull(cdb.getUser().uspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.usPrivacyOptOut(true).init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("true", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertNull(cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalse_VerifyFalseIsPassedToCdb()
      throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();
    criteo.setUsPrivacyOptOut(false);

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalseAfterFirstCall_VerifyFalseIsPassedToCdbOnTheSecondCall()
      throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    criteo.setUsPrivacyOptOut(false);
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480);

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().uspOptout());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.mopubConsent("fake_mopub_consent").init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentNotProvided_ThenProvidedAfterFirstCall_VerifyItIsPassedToCdbOnTheSecondCall()
      throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    criteo.setMopubConsent("fake_mopub_consent");
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480);

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<CdbRequest> cdbArgumentCaptor = ArgumentCaptor.forClass(CdbRequest.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    CdbRequest cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_mopub_consent", cdb.getUser().mopubConsent());
  }

  @Test
  public void whenCriteoInit_GivenMopubConsentThroughSetter_ThenCriteoCleared_ThenVerifyItIsStillPassedToCdb()
      throws Exception {
    // given
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    criteo.setMopubConsent("fake_mopub_consent");

    // when
    CriteoUtil.clearCriteo();

    // then
    Criteo.Builder builder2 = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo2 = builder2.init();
    criteo2.getBidForAdUnit(TestAdUnits.BANNER_320_480);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
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
}
