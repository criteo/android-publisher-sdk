package com.criteo.publisher.model;

import static com.criteo.publisher.CriteoUtil.clearCriteo;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import com.criteo.publisher.BuildConfig;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.network.PubSdkApi;
import javax.inject.Inject;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ConfigIntegrationTests {

  private final String CACHED_KILL_SWITCH = "CriteoCachedKillSwitch";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Inject
  private Application application;

  @Before
  public void setup() {
    givenEmptyLocalStorage();
  }

  @After
  public void tearDown() {
    givenEmptyLocalStorage();
  }

  @Test
  public void isKillSwitchEnabled_GivenEmptyLocalStorageAndNoRemoteConfig_KillSwitchIsDisabledByDefault()
      throws Exception {
    givenEmptyLocalStorage();
    givenRemoteConfigInError();

    givenInitializedCriteo();
    Config config = getConfig();

    assertFalse(config.isKillSwitchEnabled());
  }

  @Test
  public void isKillSwitchEnabled_GivenKillSwitchEnabledInLocalStorageAndNoRemoteConfig_ReturnsEnabled()
      throws Exception {
    isKillSwitchEnabled_GivenKillSwitchInLocalStorageAndNoRemoteConfig_ReturnsLocalStorageValue(
        true);
  }

  @Test
  public void isKillSwitchEnabled_GivenKillSwitchDisabledInLocalStorageAndNoRemoteConfig_ReturnsDisabled()
      throws Exception {
    isKillSwitchEnabled_GivenKillSwitchInLocalStorageAndNoRemoteConfig_ReturnsLocalStorageValue(
        false);
  }

  private void isKillSwitchEnabled_GivenKillSwitchInLocalStorageAndNoRemoteConfig_ReturnsLocalStorageValue(
      boolean isEnabled) throws Exception {
    givenKillSwitchInLocalStorage(isEnabled);
    givenRemoteConfigInError();

    givenInitializedCriteo();
    Config config = getConfig();

    assertEquals(isEnabled, config.isKillSwitchEnabled());
  }

  @Test
  public void localStorage_GivenRemoteConfigWithEnabledKillSwitch_PersistKillSwitchInLocalStorage()
      throws Exception {
    localStorage_GivenRemoteConfigWithKillSwitch_PersistKillSwitchInLocalStorage(true);
  }

  @Test
  public void localStorage_GivenRemoteConfigWithDisabledKillSwitch_PersistKillSwitchInLocalStorage()
      throws Exception {
    localStorage_GivenRemoteConfigWithKillSwitch_PersistKillSwitchInLocalStorage(false);
  }

  private void localStorage_GivenRemoteConfigWithKillSwitch_PersistKillSwitchInLocalStorage(
      boolean isEnabled) throws Exception {
    givenEmptyLocalStorage();
    givenRemoteConfigWithKillSwitch(isEnabled);

    givenInitializedCriteo();
    waitForIdleState();

    assertEquals(isEnabled, getKillSwitchInLocalStorage());
  }

  @Test
  public void localStorage_GivenRemoteConfigWithInvalidResponse_DoesNotPersistKillSwitch()
      throws Exception {
    givenEmptyLocalStorage();
    givenRemoteConfigWithResponse("{}");

    givenInitializedCriteo();
    waitForIdleState();

    assertNull(getKillSwitchInLocalStorage());
  }

  @Test
  public void localStorage_GivenNoRemoteConfig_DoesNotPersistKillSwitch() throws Exception {
    givenEmptyLocalStorage();
    givenRemoteConfigInError();

    givenInitializedCriteo();
    waitForIdleState();

    assertNull(getKillSwitchInLocalStorage());
  }

  @Test
  public void refreshConfig_GivenRemoteConfigInError_DoesNotUpdateConfig() throws Exception {
    Config config = mock(Config.class);
    doReturn(config).when(mockedDependenciesRule.getDependencyProvider()).provideConfig();

    givenRemoteConfigInError();

    givenInitializedCriteo();
    waitForIdleState();

    verify(config, never()).refreshConfig(any());
  }

  @Test
  public void refreshConfig_GivenRemoteConfigWithGoodResponse_UpdateConfig() throws Exception {
    Config config = mock(Config.class);
    when(config.isKillSwitchEnabled()).thenReturn(false);
    doReturn(config).when(mockedDependenciesRule.getDependencyProvider()).provideConfig();

    JSONObject response = mock(JSONObject.class);
    givenRemoteConfigWithResponse(response);

    givenInitializedCriteo();
    waitForIdleState();

    verify(config).refreshConfig(response);
  }

  @Test
  public void refreshConfig_GivenRemoteConfigWithGoodResponseAndKillSwitchIsEnabled_UpdateConfig()
      throws Exception {
    Config config = mock(Config.class);
    when(config.isKillSwitchEnabled()).thenReturn(true);
    doReturn(config).when(mockedDependenciesRule.getDependencyProvider()).provideConfig();

    JSONObject response = mock(JSONObject.class);
    givenRemoteConfigWithResponse(response);

    givenInitializedCriteo();
    waitForIdleState();

    verify(config).refreshConfig(response);
  }

  @Test
  public void sdkInit_GivenContext_ProvidedConfigIsUsed() throws CriteoInitException {
    clearCriteo();

    Criteo.init(application, CriteoUtil.TEST_CP_ID, null);

    verify(mockedDependenciesRule.getDependencyProvider(), atLeastOnce()).provideConfig();
  }

  @Test
  public void testConfigConstructorCachedKillSwitch() {
    givenKillSwitchInLocalStorage(true);

    // test
    Config config = new Config(context);
    // Assert that constructor hasn't cleared the cache
    assertTrue(getKillSwitchInLocalStorage());
    assertTrue(config.isKillSwitchEnabled());
  }

  @Test
  public void testRefreshConfig() throws Exception {
    Config config = new Config(context);
    // The config ctor shouldn't set the default to the shared prefs
    assertNull(getKillSwitchInLocalStorage());

    JSONObject json = new JSONObject();
    json.put("killSwitch", true);

    // test
    config.refreshConfig(json);

    assertTrue(config.isKillSwitchEnabled());
    assertTrue(getKillSwitchInLocalStorage());
  }

  @Test
  public void testRefreshConfigCachedKillSwitch() throws Exception {
    //set the killSwitch to true in sharedPrefs
    givenKillSwitchInLocalStorage(true);

    Config config = new Config(context);

    JSONObject json = new JSONObject();
    json.put("killSwitch", false);

    // test
    config.refreshConfig(json);

    assertFalse(config.isKillSwitchEnabled());
    // This should flip from the explicitly set true to false from the JSON.
    // To prevent confusion of where the 'false' value came from
    // changing the defaultValue of getBoolean to true
    assertFalse(getKillSwitchInLocalStorage());
  }

  @Test
  public void testRefreshBadJson() {
    //set the killSwitch to true in sharedPrefs
    givenKillSwitchInLocalStorage(true);

    Config config = new Config(context);

    // json intentionally left blank
    JSONObject json = new JSONObject();

    // test
    config.refreshConfig(json);

    assertTrue(config.isKillSwitchEnabled());
    // this should not flip from the explicitly set value
    // as the json doesn't have a kill switch value to overwrite
    assertTrue(getKillSwitchInLocalStorage());
  }

  private Config getConfig() {
    return mockedDependenciesRule.getDependencyProvider().provideConfig();
  }

  private void givenRemoteConfigInError() {
    givenRemoteConfigWithResponse((JSONObject) null);
  }

  private void givenRemoteConfigWithKillSwitch(boolean isEnabled) throws Exception {
    givenRemoteConfigWithResponse("{ \"killSwitch\": " + isEnabled + " }");
  }

  private void givenRemoteConfigWithResponse(String jsonResponse) throws Exception {
    givenRemoteConfigWithResponse(new JSONObject(jsonResponse));
  }

  private void givenRemoteConfigWithResponse(JSONObject configJson) {
    PubSdkApi api = givenMockedRemoteConfig();
    when(api.loadConfig(any())).thenReturn(configJson);
  }

  private PubSdkApi givenMockedRemoteConfig() {
    PubSdkApi api = mock(PubSdkApi.class);
    when(mockedDependenciesRule.getDependencyProvider().providePubSdkApi())
        .thenReturn(api);
    return api;
  }

  @Nullable
  private Boolean getKillSwitchInLocalStorage() {
    SharedPreferences sharedPreferences = getSharedPreferences();

    if (!sharedPreferences.contains(CACHED_KILL_SWITCH)) {
      return null;
    } else {
      return sharedPreferences.getBoolean(CACHED_KILL_SWITCH, false);
    }
  }

  private void givenEmptyLocalStorage() {
    SharedPreferences sharedPreferences = getSharedPreferences();

    sharedPreferences.edit()
        .remove(CACHED_KILL_SWITCH)
        .apply();
  }

  private void givenKillSwitchInLocalStorage(boolean isEnabled) {
    SharedPreferences sharedPreferences = getSharedPreferences();

    sharedPreferences.edit()
        .putBoolean(CACHED_KILL_SWITCH, isEnabled)
        .apply();
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(
        BuildConfig.pubSdkSharedPreferences,
        Context.MODE_PRIVATE);
  }

  private void waitForIdleState() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }
}
