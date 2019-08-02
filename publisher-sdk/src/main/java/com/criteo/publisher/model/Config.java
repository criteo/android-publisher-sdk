package com.criteo.publisher.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.criteo.publisher.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Config {

    private static String DEFAULT_AD_TAG_DATA_MACRO = "%%adTagData%%";
    private static String DEFAULT_DISPLAY_URL_MACRO = "%%displayUrl%%";
    private static String DEFAULT_AD_TAG_URL_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>";
    private static String DEFAULT_AD_TAG_DATA_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>";
    private static final String KILL_SWITCH = "killSwitch";
    private static final String CACHED_KILL_SWITCH = "CriteoCachedKillSwitch";
    private static final String DISPLAY_URL_MACRO_KEY = "AndroidDisplayUrlMacro";
    private static final String AD_TAG_URL_MODE_KEY = "AndroidAdTagUrlMode";
    private static final String AD_TAG_DATA_MACRO_KEY = "AndroidAdTagDataMacro";
    private static final String AD_TAG_DATA_MODE_KEY = "AndroidAdTagDataMode";
    private final boolean DEFAULT_KILL_SWITCH = false;
    private boolean killSwitch;
    private static String displayUrlMacro;
    private static String adTagUrlMode;
    private static String adTagDataMacro;
    private static String adTagDataMode;

    private static String TAG = Config.class.getSimpleName();

    public Config(Context context) {
        this.killSwitch = DEFAULT_KILL_SWITCH;
        this.displayUrlMacro = DEFAULT_DISPLAY_URL_MACRO;
        this.adTagUrlMode = DEFAULT_AD_TAG_URL_MODE;
        this.adTagDataMacro = DEFAULT_AD_TAG_DATA_MACRO;
        this.adTagDataMode = DEFAULT_AD_TAG_DATA_MODE;

        try {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
            killSwitch = sharedPref.getBoolean(CACHED_KILL_SWITCH, DEFAULT_KILL_SWITCH);
        } catch (Exception ex) {
            Log.d(TAG, "Couldn't read cached values : " + ex.getMessage());
        }
    }

    public void refreshConfig(JSONObject json, Context context) {
        if (json.has(KILL_SWITCH)) {
            try {
                boolean newKillSwitch = json.getBoolean(KILL_SWITCH);
                if (newKillSwitch != this.killSwitch) {
                    this.killSwitch = newKillSwitch;

                    SharedPreferences sharedPref = context.getSharedPreferences(
                            context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(CACHED_KILL_SWITCH, newKillSwitch);
                    editor.apply();
                }
            } catch (Exception ex) {
                Log.d(TAG, "Couldn't refresh cached values : " + ex.getMessage());
            }
        }
        this.displayUrlMacro = json.optString(DISPLAY_URL_MACRO_KEY, this.displayUrlMacro);
        this.adTagUrlMode = json.optString(AD_TAG_URL_MODE_KEY, this.adTagUrlMode);
        this.adTagDataMacro = json.optString(AD_TAG_DATA_MACRO_KEY, this.adTagDataMacro);
        this.adTagDataMode = json.optString(AD_TAG_DATA_MODE_KEY, this.adTagDataMode);
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
