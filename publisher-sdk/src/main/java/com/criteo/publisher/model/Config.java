package com.criteo.publisher.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.criteo.publisher.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Config {
    private static String TAG = Config.class.getSimpleName();
    private static final String DEFAULT_AD_TAG_DATA_MACRO = "%%adTagData%%";
    private static final String DEFAULT_DISPLAY_URL_MACRO = "%%displayUrl%%";
    private static final String DEFAULT_AD_TAG_URL_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>";
    private static final String DEFAULT_AD_TAG_DATA_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>";

    private static final String KILL_SWITCH = "killSwitch";

    private static final String KILL_SWITCH_STORAGE_KEY = "CriteoCachedKillSwitch";
    private static final String DISPLAY_URL_MACRO_KEY = "AndroidDisplayUrlMacro";
    private static final String AD_TAG_URL_MODE_KEY = "AndroidAdTagUrlMode";
    private static final String AD_TAG_DATA_MACRO_KEY = "AndroidAdTagDataMacro";
    private static final String AD_TAG_DATA_MODE_KEY = "AndroidAdTagDataMode";
    private boolean killSwitchEnabled;
    private String displayUrlMacro;
    private String adTagUrlMode;
    private String adTagDataMacro;
    private String adTagDataMode;

    @Nullable
    private Context context;

    /** used by {@link com.criteo.publisher.DummyCriteo} to create a Config object **/
    public Config() {}

    public Config(@NonNull Context context) {
        this.displayUrlMacro = DEFAULT_DISPLAY_URL_MACRO;
        this.adTagUrlMode = DEFAULT_AD_TAG_URL_MODE;
        this.adTagDataMacro = DEFAULT_AD_TAG_DATA_MACRO;
        this.adTagDataMode = DEFAULT_AD_TAG_DATA_MODE;
        this.context = context;
        this.killSwitchEnabled = readKillSwitchOrFalse(context);
    }

    public void refreshConfig(JSONObject json) {
        try {
            killSwitchEnabled = json.getBoolean(KILL_SWITCH);
            persistKillSwitch();
        } catch (JSONException e) {
            Log.d(TAG, "Couldn't read kill switch status: " + e.getMessage());
        }

        displayUrlMacro = json.optString(DISPLAY_URL_MACRO_KEY, displayUrlMacro);
        adTagUrlMode = json.optString(AD_TAG_URL_MODE_KEY, adTagUrlMode);
        adTagDataMacro = json.optString(AD_TAG_DATA_MACRO_KEY, adTagDataMacro);
        adTagDataMode = json.optString(AD_TAG_DATA_MODE_KEY, adTagDataMode);
    }

    private static boolean readKillSwitchOrFalse(@NonNull Context context) {
        try {
            return getSharedPreferences(context).getBoolean(KILL_SWITCH_STORAGE_KEY, false);
        } catch (ClassCastException ex) {
            Log.d(TAG, "Couldn't read cached values : " + ex.getMessage());
        }
        return false;
    }

    private void persistKillSwitch() {
        // FIXME(ma.chentir): the context object is effectively NonNull if this method is
        //  called, as it can only be called when creating a real Criteo instance.
        //  However, the null check is done for safety purposes. when we implement CSM,
        //  we would need to trigger an event if context is null as it would indicate a
        //  potential problem.
        if (context == null) {
            return;
        }

        Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KILL_SWITCH_STORAGE_KEY, killSwitchEnabled);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        String sharedPreferenceName = context.getString(R.string.shared_preferences);
        return context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
    }

    public boolean isKillSwitchEnabled() {
        return killSwitchEnabled;
    }

    public String getDisplayUrlMacro() {
        return displayUrlMacro;
    }

    public String getAdTagUrlMode() {
        return adTagUrlMode;
    }

    public String getAdTagDataMacro() {
        return adTagDataMacro;
    }

    public String getAdTagDataMode() {
        return adTagDataMode;
    }
}
