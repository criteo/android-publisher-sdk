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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ConfigTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  @SpyBean
  private JsonSerializer jsonSerializer;

  private Config config;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() throws Exception {
    when(sharedPreferences.getString(any(), any()))
        .thenAnswer(invocation -> invocation.getArguments()[1]);
  }

  @Test
  public void new_GivenEmptyLocalStorage_ContainsDefaultValues() {
    when(sharedPreferences.getBoolean(any(), anyBoolean()))
        .thenAnswer(invocation -> invocation.getArguments()[1]);
    when(sharedPreferences.getString(any(), any()))
        .thenAnswer(invocation -> invocation.getArguments()[1]);

    givenNewConfig();

    assertConfigContainsDefaultValues();
  }

  @Test
  public void new_GivenInvalidValueInLocalStorage_DoesNotThrowAndUseDefaultValues()
      throws Exception {
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);

    when(sharedPreferences.getBoolean(any(), anyBoolean())).thenThrow(ClassCastException.class);
    when(sharedPreferences.getString(any(), any())).thenThrow(ClassCastException.class);

    givenNewConfig();

    assertConfigContainsDefaultValues();
  }

  @Test
  public void new_GivenInvalidJsonInLocalStorage_DoesNotThrowAndUseDefaultValues()
      throws Exception {
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);

    when(sharedPreferences.getBoolean(any(), anyBoolean())).thenThrow(ClassCastException.class);
    when(sharedPreferences.getString(any(), any())).thenReturn("{");

    givenNewConfig();

    assertConfigContainsDefaultValues();
  }

  @Test
  public void refreshConfig_GivenMissingKillSwitch_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getKillSwitch()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::isKillSwitchEnabled);
  }

  @Test
  public void refreshConfig_GivenMissingUrlMacro_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidDisplayUrlMacro()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getDisplayUrlMacro);
  }

  @Test
  public void refreshConfig_GivenMissingUrlMode_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidAdTagUrlMode()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagUrlMode);
  }

  @Test
  public void refreshConfig_GivenMissingDataMacro_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidAdTagDataMacro()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagDataMacro);
  }

  @Test
  public void refreshConfig_GivenMissingDataMode_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidAdTagDataMode()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagDataMode);
  }

  @Test
  public void refreshConfig_GivenMissingCsmEnabled_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getCsmEnabled()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::isCsmEnabled);
  }

  @Test
  public void refreshConfig_GivenMissingLiveBiddingEnabled_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getLiveBiddingEnabled()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::isLiveBiddingEnabled);
  }

  @Test
  public void refreshConfig_GivenMissingLiveBiddingTimeBudget_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getLiveBiddingTimeBudgetInMillis()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getLiveBiddingTimeBudgetInMillis);
  }

  @Test
  public void refreshConfig_GivenMissingPrefetchOnInitEnabled_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getPrefetchOnInitEnabled()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::isPrefetchOnInitEnabled);
  }

  @Test
  public void refreshConfig_GivenMissingRemoteLogLevel_ItIsUnchanged() throws Exception {
    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getRemoteLogLevel()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getRemoteLogLevel);
  }

  private <T> void refreshConfig_assertItIsUnchanged(
      RemoteConfigResponse newConfig,
      Function<Config, T> projection
  ) {
    T previousValue = projection.apply(config);
    config.refreshConfig(newConfig);
    T newValue = projection.apply(config);

    assertEquals(previousValue, newValue);
  }

  @Test
  public void refreshConfig_GivenRemoteConfigAndSerializer_PersistSerializedRemoteConfig() throws Exception {
    Editor editor = mock(Editor.class);
    when(sharedPreferences.edit()).thenReturn(editor);

    givenNewConfig();

    RemoteConfigResponse newConfig = RemoteConfigResponse.create(
        true,
        "urlMacro",
        null,
        "dataMacro",
        "dataMode",
        false,
        false,
        42,
        false,
        RemoteLogLevel.ERROR
    );

    doAnswer(answerVoid((RemoteConfigResponse ignored, OutputStream outputStream) -> {
      outputStream.write("serialized".getBytes(StandardCharsets.UTF_8));
    })).when(jsonSerializer).write(eq(newConfig), any());

    config.refreshConfig(newConfig);

    InOrder inOrder = inOrder(editor);
    inOrder.verify(editor).putString("CriteoCachedConfig", "serialized");
    inOrder.verify(editor).apply();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void refreshConfig_GivenFailingSerializer_DoNotPersistAndDoNotCrash() throws Exception {
    Editor editor = mock(Editor.class);
    when(sharedPreferences.edit()).thenReturn(editor);

    givenNewConfig();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);

    doThrow(IOException.class).when(jsonSerializer).write(any(), any());

    config.refreshConfig(newConfig);

    verifyNoInteractions(editor);
  }

  @Test
  public void refreshConfig_GivenNewConfig_UpdateEverything() throws Exception {
    givenNewConfig();
    boolean killSwitchEnabled = config.isKillSwitchEnabled();
    String displayUrlMacro = config.getDisplayUrlMacro();
    String adTagUrlMode = config.getAdTagUrlMode();
    String adTagDataMacro = config.getAdTagDataMacro();
    String adTagDataMode = config.getAdTagDataMode();
    boolean csmEnabled = config.isCsmEnabled();
    boolean liveBiddingEnabled = config.isLiveBiddingEnabled();
    int liveBiddingTimeBudgetInMillis = config.getLiveBiddingTimeBudgetInMillis();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);

    config.refreshConfig(newConfig);

    assertEquals(killSwitchEnabled, !config.isKillSwitchEnabled());
    assertEquals("new_" + displayUrlMacro, config.getDisplayUrlMacro());
    assertEquals("new_" + adTagUrlMode, config.getAdTagUrlMode());
    assertEquals("new_" + adTagDataMacro, config.getAdTagDataMacro());
    assertEquals("new_" + adTagDataMode, config.getAdTagDataMode());
    assertEquals(csmEnabled, !config.isCsmEnabled());
    assertEquals(liveBiddingEnabled, !config.isLiveBiddingEnabled());
    assertEquals(1 + liveBiddingTimeBudgetInMillis, config.getLiveBiddingTimeBudgetInMillis());

  }

  private void givenNewConfig() {
    config = new Config(sharedPreferences, jsonSerializer);
  }

  private RemoteConfigResponse givenFullNewPayload(Config config) {
    RemoteConfigResponse response = mock(RemoteConfigResponse.class);
    when(response.getKillSwitch()).thenReturn(!config.isKillSwitchEnabled());
    when(response.getAndroidDisplayUrlMacro()).thenReturn("new_" + config.getDisplayUrlMacro());
    when(response.getAndroidAdTagUrlMode()).thenReturn("new_" + config.getAdTagUrlMode());
    when(response.getAndroidAdTagDataMacro()).thenReturn("new_" + config.getAdTagDataMacro());
    when(response.getAndroidAdTagDataMode()).thenReturn("new_" + config.getAdTagDataMode());
    when(response.getCsmEnabled()).thenReturn(!config.isCsmEnabled());
    when(response.getLiveBiddingEnabled()).thenReturn(!config.isLiveBiddingEnabled());
    when(response.getLiveBiddingTimeBudgetInMillis()).thenReturn(1 + config.getLiveBiddingTimeBudgetInMillis());
    when(response.getPrefetchOnInitEnabled()).thenReturn(!config.isPrefetchOnInitEnabled());

    // Get any value that is not the one set in the given config
    RemoteLogLevel otherLogLevel = Arrays.stream(RemoteLogLevel.values())
        .filter(logLevel -> logLevel != config.getRemoteLogLevel())
        .findFirst().get();

    when(response.getRemoteLogLevel()).thenReturn(otherLogLevel);
    return response;
  }

  private void assertConfigContainsDefaultValues() {
    assertFalse(config.isKillSwitchEnabled());
    assertEquals("%%adTagData%%", config.getAdTagDataMacro());
    assertEquals("%%displayUrl%%", config.getDisplayUrlMacro());
    assertEquals(
        "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>",
        config.getAdTagUrlMode());
    assertEquals(
        "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>",
        config.getAdTagDataMode());
    assertTrue(config.isCsmEnabled());
    assertTrue(config.isLiveBiddingEnabled());
    assertEquals(8000, config.getLiveBiddingTimeBudgetInMillis());
    assertFalse(config.isPrefetchOnInitEnabled());
  }

}