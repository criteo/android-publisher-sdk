package com.criteo.publisher.model;

import org.json.JSONObject;

public class Config {

    public static final String DEFAULT_AD_TAG_DATA_MACRO = "%%adTagData%%";
    public static final String DEFAULT_DISPLAY_URL_MACRO = "%%displayUrl%%";
    public static final String DEFAULT_AD_TAG_URL_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>";
    public static final String DEFAULT_AD_TAG_DATA_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>";
    private static final String KILL_SWITCH = "killSwitch";
    private static final String DISPLAY_URL_MACRO_KEY = "AndroidDisplayUrlMacro";
    private static final String AD_TAG_URL_MODE_KEY = "AndroidAdTagUrlMode";
    private static final String AD_TAG_DATA_MACRO_KEY = "AndroidAdTagDataMacro";
    private static final String AD_TAG_DATA_MODE_KEY = "AndroidAdTagDataMode";
    private boolean killSwitch;
    private static String displayUrlMacro;
    private static String adTagUrlMode;
    private static String adTagDataMacro;
    private static String adTagDataMode;

    public Config(JSONObject json) {
        this.killSwitch = json.optBoolean(KILL_SWITCH, false);
        this.displayUrlMacro = json.optString(DISPLAY_URL_MACRO_KEY, DEFAULT_DISPLAY_URL_MACRO);
        this.adTagUrlMode = json.optString(AD_TAG_URL_MODE_KEY, DEFAULT_AD_TAG_URL_MODE);
        this.adTagDataMacro = json.optString(AD_TAG_DATA_MACRO_KEY, DEFAULT_AD_TAG_DATA_MACRO);
        this.adTagDataMode = json.optString(AD_TAG_DATA_MODE_KEY, DEFAULT_AD_TAG_DATA_MODE);
    }

    public boolean isKillSwitch() {
        return killSwitch;
    }

    public static String getDisplayUrlMacro() {
        return displayUrlMacro;
    }

    public static String getAdTagUrlMode() {
        return adTagUrlMode;
    }

    public static String getAdTagDataMacro() {
        return adTagDataMacro;
    }

    public static String getAdTagDataMode() {
        return adTagDataMode;
    }
}
