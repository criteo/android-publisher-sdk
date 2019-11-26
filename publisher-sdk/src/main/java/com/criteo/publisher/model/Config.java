package com.criteo.publisher.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.criteo.publisher.R;

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

        try {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);

            killSwitchEnabled = sharedPref.getBoolean(KILL_SWITCH_STORAGE_KEY, false);
        } catch (Exception ex) {
            Log.d(TAG, "Couldn't read cached values : " + ex.getMessage());
        }
    }

    public void refreshConfig(JSONObject json) {
        if (json.has(KILL_SWITCH)) {
            try {
                boolean newKillSwitch = json.getBoolean(KILL_SWITCH);
                if (newKillSwitch != this.killSwitchEnabled) {
                    this.killSwitchEnabled = newKillSwitch;

                    // FIXME(ma.chentir): the context object is effectively NonNull if this method is
                    //  called, as it can only be called when creating a real Criteo instance.
                    //  However, the null check is done for safety purposes. when we implement CSM,
                    //  we would need to trigger an event if context is null as it would indicate a
                    //  potential problem.
                    if (context != null) {
                        SharedPreferences sharedPref = context.getSharedPreferences(
                            context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(KILL_SWITCH_STORAGE_KEY, newKillSwitch);
                        editor.apply();
                    }

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
