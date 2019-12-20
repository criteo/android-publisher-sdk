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
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.network.PubSdkApi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class USPrivacyFunctionalTest {
  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Mock
  private PubSdkApi pubSdkApi;

  private SharedPreferences defaultSharedPreferences;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
    defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());

    doReturn(pubSdkApi).when(dependencyProvider).providePubSdkApi(any());
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

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    Cdb cdb = cdbArgumentCaptor.getValue();
    assertEquals("fake_iab_usp", cdb.getUser().getUspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspIabEmpty_VerifyItIsNotPassedToCdb() throws Exception {
    writeIntoDefaultSharedPrefs("IABUSPrivacy_String", null);

    CriteoUtil.givenInitializedCriteo(TestAdUnits.BANNER_320_50);
    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    Cdb cdb = cdbArgumentCaptor.getValue();
    Assert.assertNull( cdb.getUser().getUspIab());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutNotEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.usPrivacyOptOut(true).init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    Cdb cdb = cdbArgumentCaptor.getValue();
    assertEquals("true", cdb.getUser().getUspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutEmpty_VerifyItIsPassedToCdb() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    builder.init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    Cdb cdb = cdbArgumentCaptor.getValue();
    assertNull(cdb.getUser().getUspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalse_VerifyFalseIsPassedToCdb() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();
    criteo.setUsPrivacyOptOut(false);

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);
    verify(pubSdkApi).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    Cdb cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().getUspOptout());
  }

  @Test
  public void whenCriteoInit_GivenUspOptoutTrue_ThenChangedToFalseAfterFirstCall_VerifyFalseIsPassedToCdbOnTheSecondCall() throws Exception {
    Criteo.Builder builder = CriteoUtil.getCriteoBuilder(TestAdUnits.BANNER_320_50);
    Criteo criteo = builder.usPrivacyOptOut(true).init();

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    criteo.setUsPrivacyOptOut(false);
    criteo.getBidForAdUnit(TestAdUnits.BANNER_320_480);

    ThreadingUtil.waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());

    ArgumentCaptor<Cdb> cdbArgumentCaptor = ArgumentCaptor.forClass(Cdb.class);
    verify(pubSdkApi, times(2)).loadCdb(cdbArgumentCaptor.capture(), any(String.class));

    Cdb cdb = cdbArgumentCaptor.getValue();
    assertEquals("false", cdb.getUser().getUspOptout());
  }

  private void writeIntoDefaultSharedPrefs(String key, String value) {
    Editor edit = defaultSharedPreferences.edit();
    edit.putString(key, value);
    edit.commit();
  }
}
