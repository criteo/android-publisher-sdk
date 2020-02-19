package com.criteo.publisher.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.BuildConfig;
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

  // NOTE: This entire object is not at all thread-safe, but except the kill switch, other config
  //  are only accessed at display time. As they are only updated during SDK init, before any bids
  //  are registered. Then we may consider that, by usage, this object is thread-safe.
  private volatile boolean killSwitchEnabled;
  private String displayUrlMacro;
  private String adTagUrlMode;
  private String adTagDataMacro;
  private String adTagDataMode;

  @Nullable
  private Context context;

  /**
   * used by {@link com.criteo.publisher.DummyCriteo} to create a Config object
   **/
  public Config() {
  }

  public Config(@NonNull Context context) {
    this.displayUrlMacro = DEFAULT_DISPLAY_URL_MACRO;
    this.adTagUrlMode = DEFAULT_AD_TAG_URL_MODE;
    this.adTagDataMacro = DEFAULT_AD_TAG_DATA_MACRO;
    this.adTagDataMode = DEFAULT_AD_TAG_DATA_MODE;
    this.context = context;
    this.killSwitchEnabled = readKillSwitchOrFalse(context);
  }

  /**
   * Parse kill switch from given JSON payload.
   * <p>
   * It is expected to read a boolean value at the {@link #KILL_SWITCH} element. If none is found or
   * non boolean value is found, <code>null</code> is returned.
   *
   * @param json payload to read from
   * @return existing kill switch status or <code>null</code> if not readable
   */
  @Nullable
  public static Boolean parseKillSwitch(@Nullable JSONObject json) {
    if (json == null) {
      return null;
    }

    try {
      return json.getBoolean(KILL_SWITCH);
    } catch (JSONException e) {
      return null;
    }
  }

  public void refreshConfig(@NonNull JSONObject json) {
    Boolean newKillSwitch = parseKillSwitch(json);
    if (newKillSwitch != null) {
      killSwitchEnabled = newKillSwitch;
      persistKillSwitch();
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
    return context.getSharedPreferences(
        BuildConfig.PUBSDK_SHARED_PREFERENCES,
        Context.MODE_PRIVATE);
  }

  public boolean isKillSwitchEnabled() {
    return killSwitchEnabled;
  }

  @NonNull
  public String getDisplayUrlMacro() {
    return displayUrlMacro;
  }

  @NonNull
  public String getAdTagUrlMode() {
    return adTagUrlMode;
  }

  @NonNull
  public String getAdTagDataMacro() {
    return adTagDataMacro;
  }

  @NonNull
  public String getAdTagDataMode() {
    return adTagDataMode;
  }
}
