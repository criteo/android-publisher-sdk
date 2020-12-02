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

package com.criteo.publisher.model;

import static com.criteo.publisher.CriteoUtil.clearCriteo;
import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
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
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.CriteoUtil;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.RemoteConfigResponse.RemoteLogLevel;
import com.criteo.publisher.network.PubSdkApi;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ConfigIntegrationTests {

  private final String CACHED_CONFIG = "CriteoCachedConfig";

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private JsonSerializer jsonSerializer;

  @Inject
  private Application application;

  @Inject
  private SharedPreferences sharedPreferences;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @Before
  public void setup() throws Exception {
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
    waitForIdleState();
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
    waitForIdleState();
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
    givenRemoteConfigResponseWithKillSwitch(isEnabled);

    givenInitializedCriteo();
    waitForIdleState();

    assertEquals(isEnabled, getKillSwitchInLocalStorage());
  }

  @Test
  public void localStorage_GivenNullKillSwitch_DoesNotPersistKillSwitch()
      throws Exception {
    givenEmptyLocalStorage();
    givenRemoteConfigResponseWithKillSwitch(null);

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

    RemoteConfigResponse response = mock(RemoteConfigResponse.class);
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

    RemoteConfigResponse response = mock(RemoteConfigResponse.class);
    givenRemoteConfigWithResponse(response);

    givenInitializedCriteo();
    waitForIdleState();

    verify(config).refreshConfig(response);
  }

  @Test
  public void sdkInit_GivenContext_ProvidedConfigIsUsed() throws CriteoInitException {
    clearCriteo();

    new Criteo.Builder(application, CriteoUtil.TEST_CP_ID).init();

    verify(mockedDependenciesRule.getDependencyProvider(), atLeastOnce()).provideConfig();
  }

  @Test
  public void testConfigConstructorCachedRemoteConfig() throws Exception {
    RemoteConfigResponse remoteConfig = createRemoteConfigWithKillSwitch(true);

    givenRemoteConfigInLocalStorage(remoteConfig);

    // test
    Config config = createConfig();

    // Assert that constructor hasn't cleared the cache
    assertEquals(remoteConfig, getRemoteConfigInLocalStorage());
    assertTrue(config.isKillSwitchEnabled());
  }

  @Test
  public void testRefreshConfig() throws Exception {
    Config config = createConfig();
    // The config ctor shouldn't set the default to the shared prefs
    assertNull(getKillSwitchInLocalStorage());

    RemoteConfigResponse response = createRemoteConfigWithKillSwitch(true);

    // test
    config.refreshConfig(response);

    assertTrue(config.isKillSwitchEnabled());
    assertTrue(getKillSwitchInLocalStorage());
  }

  @Test
  public void refreshConfig_GivenKillSwitchInStorageAndGivenNewKillSwitchInRemoteConfig_PersistNewKillSwitch()
      throws Exception {
    //set the killSwitch to true in sharedPrefs
    givenKillSwitchInLocalStorage(true);

    Config config = createConfig();

    RemoteConfigResponse response = createRemoteConfigWithKillSwitch(false);

    // test
    config.refreshConfig(response);

    assertFalse(config.isKillSwitchEnabled());
    // This should flip from the explicitly set true to false from the JSON.
    // To prevent confusion of where the 'false' value came from
    // changing the defaultValue of getBoolean to true
    assertFalse(getKillSwitchInLocalStorage());
  }

  @Test
  public void refreshConfig_GivenKillSwitchInStorageAndGivenNullKillSwitchInRemoteConfig_DoNotPersistNullKillSwitch() throws Exception {
    //set the killSwitch to true in sharedPrefs
    givenKillSwitchInLocalStorage(true);

    Config config = createConfig();

    RemoteConfigResponse response = createRemoteConfigWithKillSwitch(null);

    // test
    config.refreshConfig(response);

    assertTrue(config.isKillSwitchEnabled());

    // this should not flip from the explicitly set value
    // as the json doesn't have a kill switch value to overwrite
    assertTrue(getKillSwitchInLocalStorage());
  }

  @Test
  public void refreshConfig_GivenEmptyLocalStorageAndEmptyRemoteConfig_KeepDefaultValues() throws Exception {
    givenEmptyLocalStorage();

    Config config = createConfig();
    String defaultUrlMode = config.getAdTagUrlMode();

    RemoteConfigResponse emptyRemoteConfig = RemoteConfigResponse.createEmpty();

    config.refreshConfig(emptyRemoteConfig);

    assertEquals(defaultUrlMode, config.getAdTagUrlMode());
  }

  @Test
  public void refreshConfig_GivenNotEmptyLocalStorageAndEmptyRemoteConfig_KeepPreviousValuesAndDoNotPersistEmptyValues() throws Exception {
    RemoteConfigResponse persistedConfig = RemoteConfigResponse.create(
        true,
        "macro1",
        "mode1",
        "macro2",
        "mode2",
        false,
        true,
        1337,
        true,
        RemoteLogLevel.DEBUG
    );

    givenRemoteConfigInLocalStorage(persistedConfig);

    Config config = createConfig();

    RemoteConfigResponse emptyRemoteConfig = RemoteConfigResponse.createEmpty();

    config.refreshConfig(emptyRemoteConfig);

    assertEquals("mode1", config.getAdTagUrlMode());
    assertEquals(persistedConfig, getRemoteConfigInLocalStorage());
  }

  @Test
  public void refreshConfig_GivenNotEmptyLocalStorageAndPartialRemoteConfig_OverrideNewNonNullValuesPersistMergedConfig() throws Exception {
    RemoteConfigResponse oldPersistedConfig = RemoteConfigResponse.create(
        true,
        null,
        "mode1",
        null,
        "mode2",
        null,
        null,
        null,
        null,
        null
    );

    RemoteConfigResponse remoteConfig = RemoteConfigResponse.create(
        null,
        null,
        "overriddenMode1",
        "overriddenMacro2",
        null,
        false,
        true,
        42,
        false,
        RemoteLogLevel.INFO
    );

    RemoteConfigResponse expectedRemoteConfig = RemoteConfigResponse.create(
        true,
        null,
        "overriddenMode1",
        "overriddenMacro2",
        "mode2",
        false,
        true,
        42,
        false,
        RemoteLogLevel.INFO
    );

    givenRemoteConfigInLocalStorage(oldPersistedConfig);

    Config config = createConfig();

    config.refreshConfig(remoteConfig);

    assertEquals("overriddenMacro2", config.getAdTagDataMacro());
    assertEquals("mode2", config.getAdTagDataMode());
    assertEquals(expectedRemoteConfig, getRemoteConfigInLocalStorage());
  }

  private Config getConfig() {
    return mockedDependenciesRule.getDependencyProvider().provideConfig();
  }

  private RemoteConfigResponse createRemoteConfigWithKillSwitch(Boolean isEnabled) {
    return RemoteConfigResponse.createEmpty().withKillSwitch(isEnabled);
  }

  private void givenRemoteConfigInError() throws IOException {
    doReturn(false).when(buildConfigWrapper).preconditionThrowsOnException();
    PubSdkApi api = givenMockedRemoteConfig();
    when(api.loadConfig(any())).thenThrow(IOException.class);
  }

  private void givenRemoteConfigResponseWithKillSwitch(Boolean isEnabled) throws Exception {
    RemoteConfigResponse response = createRemoteConfigWithKillSwitch(isEnabled);
    givenRemoteConfigWithResponse(response);
  }

  private void givenRemoteConfigWithResponse(RemoteConfigResponse response) throws IOException {
    PubSdkApi api = givenMockedRemoteConfig();
    when(api.loadConfig(any())).thenReturn(response);
  }

  private PubSdkApi givenMockedRemoteConfig() {
    PubSdkApi api = mock(PubSdkApi.class);
    when(mockedDependenciesRule.getDependencyProvider().providePubSdkApi()).thenReturn(api);
    return api;
  }

  @Nullable
  private Boolean getKillSwitchInLocalStorage() throws Exception {
    RemoteConfigResponse remoteConfig = getRemoteConfigInLocalStorage();
    if (remoteConfig != null) {
      return remoteConfig.getKillSwitch();
    }
    return null;
  }

  @Nullable
  private RemoteConfigResponse getRemoteConfigInLocalStorage() throws Exception {
    if (!sharedPreferences.contains(CACHED_CONFIG)) {
      return null;
    } else {
      String json = sharedPreferences.getString(CACHED_CONFIG, "");
      ByteArrayInputStream jsonBais = new ByteArrayInputStream(json.getBytes("UTF-8"));
      return jsonSerializer.read(RemoteConfigResponse.class, jsonBais);
    }
  }

  private void givenEmptyLocalStorage() {
    sharedPreferences.edit().clear().apply();
  }

  private void givenKillSwitchInLocalStorage(boolean isEnabled) throws Exception {
    RemoteConfigResponse remoteConfig = createRemoteConfigWithKillSwitch(isEnabled);
    givenRemoteConfigInLocalStorage(remoteConfig);
  }

  private void givenRemoteConfigInLocalStorage(RemoteConfigResponse remoteConfigResponse) throws Exception{
    String json;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      jsonSerializer.write(remoteConfigResponse, baos);
      json = baos.toString("UTF-8");
    }

    sharedPreferences.edit()
        .putString(CACHED_CONFIG, json)
        .apply();
  }

  private void waitForIdleState() {
    mockedDependenciesRule.waitForIdleState();
  }

  @NonNull
  private Config createConfig() {
    return new Config(sharedPreferences, jsonSerializer);
  }
}
