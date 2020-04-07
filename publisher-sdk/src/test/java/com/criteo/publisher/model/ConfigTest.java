package com.criteo.publisher.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigTest {

  private Config config;

  @Mock
  private Context context;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(context.getSharedPreferences(any(), eq(Context.MODE_PRIVATE)))
        .thenReturn(sharedPreferences);
  }

  @Test
  public void new_GivenEmptyLocalStorage_ContainsDefaultValues() {
    when(sharedPreferences.getBoolean(any(), anyBoolean()))
        .thenAnswer(invocation -> invocation.getArguments()[1]);

    config = new Config(context);

    assertConfigContainsDefaultValues();
  }

  @Test
  public void new_GivenInvalidValueInLocalStorage_DoesNotThrowAndUseDefaultValues()
      throws Exception {
    when(sharedPreferences.getBoolean(any(), anyBoolean())).thenThrow(ClassCastException.class);

    config = new Config(context);

    assertConfigContainsDefaultValues();
  }

  @Test
  public void isKillSwitchEnabled_GivenKillSwitchEnabledInLocalStorage_ReturnsEnabled() {
    givenKillSwitchInLocalStorage(true);

    config = new Config(context);

    assertTrue(config.isKillSwitchEnabled());
  }

  @Test
  public void isKillSwitchEnabled_GivenKillSwitchDisabledInLocalStorage_ReturnsDisabled() {
    givenKillSwitchInLocalStorage(false);

    config = new Config(context);

    assertFalse(config.isKillSwitchEnabled());
  }

  @Test
  public void refreshConfig_GivenMissingKillSwitch_ItIsUnchanged() throws Exception {
    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getKillSwitch()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::isKillSwitchEnabled);
  }

  @Test
  public void refreshConfig_GivenMissingUrlMacro_ItIsUnchanged() throws Exception {
    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidDisplayUrlMacro()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getDisplayUrlMacro);
  }

  @Test
  public void refreshConfig_GivenMissingUrlMode_ItIsUnchanged() throws Exception {
    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidAdTagUrlMode()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagUrlMode);
  }

  @Test
  public void refreshConfig_GivenMissingDataMacro_ItIsUnchanged() throws Exception {
    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidAdTagDataMacro()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagDataMacro);
  }

  @Test
  public void refreshConfig_GivenMissingDataMode_ItIsUnchanged() throws Exception {
    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getAndroidAdTagDataMode()).thenReturn(null);

    refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagDataMode);
  }

  private <T> void refreshConfig_assertItIsUnchanged(RemoteConfigResponse newConfig,
      Function<Config, T> projection) {
    T previousValue = projection.apply(config);
    config.refreshConfig(newConfig);
    T newValue = projection.apply(config);

    assertEquals(previousValue, newValue);
  }

  @Test
  public void refreshConfig_GivenEnabledKillSwitch_ItIsPersisted() throws Exception {
    Editor editor = mock(Editor.class);
    when(sharedPreferences.edit()).thenReturn(editor);

    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getKillSwitch()).thenReturn(true);

    config.refreshConfig(newConfig);

    InOrder inOrder = inOrder(editor);
    inOrder.verify(editor).putBoolean("CriteoCachedKillSwitch", true);
    inOrder.verify(editor).apply();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void refreshConfig_GivenNullKillSwitch_ItIsNotPersisted() throws Exception {
    config = new Config(context);

    RemoteConfigResponse newConfig = givenFullNewPayload(config);
    when(newConfig.getKillSwitch()).thenReturn(null);

    clearInvocations(sharedPreferences);
    config.refreshConfig(newConfig);

    verifyZeroInteractions(sharedPreferences);
  }

  @Test
  public void refreshConfig_GivenNewConfig_UpdateEverything() throws Exception {
    config = new Config(context);
    boolean killSwitchEnabled = config.isKillSwitchEnabled();
    String displayUrlMacro = config.getDisplayUrlMacro();
    String adTagUrlMode = config.getAdTagUrlMode();
    String adTagDataMacro = config.getAdTagDataMacro();
    String adTagDataMode = config.getAdTagDataMode();

    RemoteConfigResponse newConfig = givenFullNewPayload(config);

    config.refreshConfig(newConfig);

    assertEquals(!config.isKillSwitchEnabled(), killSwitchEnabled);
    assertEquals("new_" + displayUrlMacro, config.getDisplayUrlMacro());
    assertEquals("new_" + adTagUrlMode, config.getAdTagUrlMode());
    assertEquals("new_" + adTagDataMacro, config.getAdTagDataMacro());
    assertEquals("new_" + adTagDataMode, config.getAdTagDataMode());
  }

  private RemoteConfigResponse givenFullNewPayload(Config config) {
    RemoteConfigResponse response = mock(RemoteConfigResponse.class);
    when(response.getKillSwitch()).thenReturn(!config.isKillSwitchEnabled());
    when(response.getAndroidDisplayUrlMacro()).thenReturn("new_" + config.getDisplayUrlMacro());
    when(response.getAndroidAdTagUrlMode()).thenReturn("new_" + config.getAdTagUrlMode());
    when(response.getAndroidAdTagDataMacro()).thenReturn("new_" + config.getAdTagDataMacro());
    when(response.getAndroidAdTagDataMode()).thenReturn("new_" + config.getAdTagDataMode());
    return response;
  }

  private void givenKillSwitchInLocalStorage(boolean isEnabled) {
    when(sharedPreferences.getBoolean(eq("CriteoCachedKillSwitch"), anyBoolean()))
        .thenReturn(isEnabled);
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
  }

}