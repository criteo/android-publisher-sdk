package com.criteo.publisher.model;

import static org.json.JSONObject.quote;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.function.Function;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigTest {

    private Config config;

    @Mock
    private Context context;

    @Mock
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(context.getSharedPreferences(any(), anyInt())).thenReturn(sharedPreferences);
    }

    @Test
    public void new_GivenEmptyLocalStorage_ContainsDefaultValues() {
        when(sharedPreferences.getBoolean(any(), anyBoolean()))
            .thenAnswer(invocation -> invocation.getArguments()[1]);

        config = new Config(context);

        assertConfigContainsDefaultValues();
    }

    @Test
    public void new_GivenInvalidValueInLocalStorage_DoesNotThrowAndUseDefaultValues() throws Exception {
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

        JSONObject newConfig = givenFullNewPayload(config);
        newConfig.remove("killSwitch");

        refreshConfig_assertItIsUnchanged(newConfig, Config::isKillSwitchEnabled);
    }

    @Test
    public void refreshConfig_GivenMissingUrlMacro_ItIsUnchanged() throws Exception {
        config = new Config(context);

        JSONObject newConfig = givenFullNewPayload(config);
        newConfig.remove("AndroidDisplayUrlMacro");

        refreshConfig_assertItIsUnchanged(newConfig, Config::getDisplayUrlMacro);
    }

    @Test
    public void refreshConfig_GivenMissingUrlMode_ItIsUnchanged() throws Exception {
        config = new Config(context);

        JSONObject newConfig = givenFullNewPayload(config);
        newConfig.remove("AndroidAdTagUrlMode");

        refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagUrlMode);
    }

    @Test
    public void refreshConfig_GivenMissingDataMacro_ItIsUnchanged() throws Exception {
        config = new Config(context);

        JSONObject newConfig = givenFullNewPayload(config);
        newConfig.remove("AndroidAdTagDataMacro");

        refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagDataMacro);
    }

    @Test
    public void refreshConfig_GivenMissingDataMode_ItIsUnchanged() throws Exception {
        config = new Config(context);

        JSONObject newConfig = givenFullNewPayload(config);
        newConfig.remove("AndroidAdTagDataMode");

        refreshConfig_assertItIsUnchanged(newConfig, Config::getAdTagDataMode);
    }

    @Test
    public void refreshConfig_GivenInvalidKillSwitch_ItIsUnchanged() throws Exception {
        config = new Config(context);

        JSONObject newConfig = givenFullNewPayload(config);
        newConfig.put("killSwitch", "not a boolean");

        refreshConfig_assertItIsUnchanged(newConfig, Config::isKillSwitchEnabled);
    }

    private <T> void refreshConfig_assertItIsUnchanged(JSONObject newConfig, Function<Config, T> projection) {
        T previousValue = projection.apply(config);
        config.refreshConfig(newConfig);
        T newValue = projection.apply(config);

        assertEquals(previousValue, newValue);
    }

    @Test
    public void testRefreshConfig() throws Exception {
        givenKillSwitchInLocalStorage(false);

        config = new Config(context);

        String newDisplayUrlMacro = "%%newDisplayUrl%%";
        JSONObject json = new JSONObject();
        json.put("killSwitch", true);
        json.put("AndroidDisplayUrlMacro", newDisplayUrlMacro);

        config.refreshConfig(json);

        assertTrue(config.isKillSwitchEnabled());
        assertNotNull(config.getAdTagUrlMode());
        assertEquals(newDisplayUrlMacro, config.getDisplayUrlMacro());
    }

    private JSONObject givenFullNewPayload(Config config) throws Exception {
        return new JSONObject("{\n"
            + "  \"killSwitch\": " + !config.isKillSwitchEnabled() + ",\n"
            + "  \"AndroidDisplayUrlMacro\": " + quote("new_" + config.getDisplayUrlMacro()) + ",\n"
            + "  \"AndroidAdTagUrlMode\": " + quote("new_" + config.getAdTagUrlMode()) + ",\n"
            + "  \"AndroidAdTagDataMacro\": " + quote("new_" + config.getAdTagDataMacro()) + ",\n"
            + "  \"AndroidAdTagDataMode\": " + quote("new_" + config.getAdTagDataMode()) + ",\n"
            + "  \"iOSDisplayUrlMacro\": \"not used\",\n"
            + "  \"iOSWidthMacro\": \"not used\",\n"
            + "  \"iOSAdTagUrlMode\": \"not used\"\n"
            + "}");
    }

    private void givenKillSwitchInLocalStorage(boolean isEnabled) {
        when(sharedPreferences.getBoolean(eq("CriteoCachedKillSwitch"), anyBoolean())).thenReturn(isEnabled);
    }

    private void assertConfigContainsDefaultValues() {
        assertFalse(config.isKillSwitchEnabled());
        assertEquals("%%adTagData%%", config.getAdTagDataMacro());
        assertEquals("%%displayUrl%%", config.getDisplayUrlMacro());
        assertEquals("<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>", config.getAdTagUrlMode());
        assertEquals("<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>", config.getAdTagDataMode());
    }

}