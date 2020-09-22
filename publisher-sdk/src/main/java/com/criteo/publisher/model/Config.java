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

import static com.criteo.publisher.util.ObjectUtils.getOrElse;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.util.JsonSerializer;
import com.criteo.publisher.util.SafeSharedPreferences;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Config {

  private static final String TAG = Config.class.getSimpleName();

  /**
   * Key in local storage where kill switch was persisted before. We keep compatibility with such
   * persisted data, but it is overridden by new persistence strategies.
   *
   * @see #CONFIG_STORAGE_KEY
   */
  @Deprecated
  private static final String OLD_KILL_SWITCH_STORAGE_KEY = "CriteoCachedKillSwitch";

  /**
   * Key in local storage where all configuration from remote is persisted.
   */
  private static final String CONFIG_STORAGE_KEY = "CriteoCachedConfig";

  private static class DefaultConfig {

    private static final boolean KILL_SWITCH = false;
    private static final String DISPLAY_URL_MACRO = "%%displayUrl%%";
    private static final String AD_TAG_URL_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>";
    private static final String AD_TAG_DATA_MACRO = "%%adTagData%%";
    private static final String AD_TAG_DATA_MODE = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>";
    private static final boolean CSM_ENABLED = true;
    private static final boolean LIVE_BIDDING_ENABLED = false;

  }

  // NOTE: This entire object is not at all thread-safe, but except the kill switch, other config
  //  are only accessed at display time. As they are only updated during SDK init, before any bids
  //  are registered. Then we may consider that, by usage, this object is thread-safe.
  @NonNull
  private volatile RemoteConfigResponse cachedRemoteConfig;

  @Nullable
  private final SharedPreferences sharedPreferences;

  @Nullable
  private final JsonSerializer jsonSerializer;

  /**
   * used by {@link com.criteo.publisher.DummyCriteo} to create a Config object
   **/
  public Config() {
    this.sharedPreferences = null;
    this.jsonSerializer = null;
    this.cachedRemoteConfig = RemoteConfigResponse.createEmpty();
  }

  public Config(
      @NonNull SharedPreferences sharedPreferences,
      @NonNull JsonSerializer jsonSerializer
  ) {
    this.sharedPreferences = sharedPreferences;
    this.jsonSerializer = jsonSerializer;
    this.cachedRemoteConfig = readConfigOrEmpty();
  }

  @NonNull
  private RemoteConfigResponse readConfigOrEmpty() {
    RemoteConfigResponse config = RemoteConfigResponse.createEmpty();

    if (sharedPreferences == null || jsonSerializer == null) {
      return config;
    }

    SafeSharedPreferences safeSharedPreferences = new SafeSharedPreferences(sharedPreferences);

    // Keep compatibility with old kill switches stored in local storage
    if (sharedPreferences.contains(OLD_KILL_SWITCH_STORAGE_KEY)) {
      boolean killSwitch = safeSharedPreferences.getBoolean(
          OLD_KILL_SWITCH_STORAGE_KEY,
          DefaultConfig.KILL_SWITCH
      );

      config = config.withKillSwitch(killSwitch);
    }

    String remoteConfigJson = safeSharedPreferences.getString(CONFIG_STORAGE_KEY, "{}");
    byte[] remoteConfigJsonBytes = remoteConfigJson.getBytes(Charset.forName("UTF-8"));

    RemoteConfigResponse readConfig;
    try (InputStream inputStream = new ByteArrayInputStream(remoteConfigJsonBytes)) {
      readConfig = jsonSerializer.read(RemoteConfigResponse.class, inputStream);
    } catch (IOException e) {
      Log.d(TAG, "Couldn't read cached values", e);
      return config;
    }

    return mergeRemoteConfig(config, readConfig);
  }

  @NonNull
  private RemoteConfigResponse mergeRemoteConfig(
      @NonNull RemoteConfigResponse baseRemoteConfig,
      @NonNull RemoteConfigResponse overrideRemoteConfig
  ) {
    return RemoteConfigResponse.create(
        getOrElse(
            overrideRemoteConfig.getKillSwitch(),
            baseRemoteConfig.getKillSwitch()
        ),
        getOrElse(
            overrideRemoteConfig.getAndroidDisplayUrlMacro(),
            baseRemoteConfig.getAndroidDisplayUrlMacro()
        ),
        getOrElse(
            overrideRemoteConfig.getAndroidAdTagUrlMode(),
            baseRemoteConfig.getAndroidAdTagUrlMode()
        ),
        getOrElse(
            overrideRemoteConfig.getAndroidAdTagDataMacro(),
            baseRemoteConfig.getAndroidAdTagDataMacro()
        ),
        getOrElse(
            overrideRemoteConfig.getAndroidAdTagDataMode(),
            baseRemoteConfig.getAndroidAdTagDataMode()
        ),
        getOrElse(
            overrideRemoteConfig.getCsmEnabled(),
            baseRemoteConfig.getCsmEnabled()
        ),
        getOrElse(
            overrideRemoteConfig.getLiveBiddingEnabled(),
            baseRemoteConfig.getLiveBiddingEnabled()
        )
    );
  }

  public void refreshConfig(@NonNull RemoteConfigResponse response) {
    cachedRemoteConfig = mergeRemoteConfig(cachedRemoteConfig, response);
    persistRemoteConfig(cachedRemoteConfig);
  }

  private void persistRemoteConfig(@NonNull RemoteConfigResponse response) {
    // FIXME(ma.chentir): the context object is effectively NonNull if this method is
    //  called, as it can only be called when creating a real Criteo instance.
    //  However, the null check is done for safety purposes. when we implement CSM,
    //  we would need to trigger an event if context is null as it would indicate a
    //  potential problem.
    if (sharedPreferences == null || jsonSerializer == null) {
      return;
    }

    String remoteConfigJson;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      jsonSerializer.write(response, baos);
      remoteConfigJson = new String(baos.toByteArray(), Charset.forName("UTF-8"));
    } catch (Exception e) {
      Log.d(TAG, "Couldn't persist values", e);
      return;
    }

    Editor editor = sharedPreferences.edit();
    editor.putString(CONFIG_STORAGE_KEY, remoteConfigJson);
    editor.apply();
  }

  public boolean isKillSwitchEnabled() {
    return getOrElse(
        cachedRemoteConfig.getKillSwitch(),
        DefaultConfig.KILL_SWITCH
    );
  }

  /**
   * Return <code>true</code> to indicate if the CSM feature is activated. Else <code>false</code>
   * is returned.
   */
  public boolean isCsmEnabled() {
    return getOrElse(
        cachedRemoteConfig.getCsmEnabled(),
        DefaultConfig.CSM_ENABLED
    );
  }

  /**
   * Return <code>true</code> to indicate if the live-bidding is enabled, <code>false</code>
   * otherwise.
   */
  public boolean isLiveBiddingEnabled() {
    return getOrElse(
        cachedRemoteConfig.getLiveBiddingEnabled(),
        DefaultConfig.LIVE_BIDDING_ENABLED
    );
  }

  @NonNull
  public String getDisplayUrlMacro() {
    return getOrElse(
        cachedRemoteConfig.getAndroidDisplayUrlMacro(),
        DefaultConfig.DISPLAY_URL_MACRO
    );
  }

  @NonNull
  public String getAdTagUrlMode() {
    return getOrElse(
        cachedRemoteConfig.getAndroidAdTagUrlMode(),
        DefaultConfig.AD_TAG_URL_MODE
    );
  }

  @NonNull
  public String getAdTagDataMacro() {
    return getOrElse(
        cachedRemoteConfig.getAndroidAdTagDataMacro(),
        DefaultConfig.AD_TAG_DATA_MACRO
    );
  }

  @NonNull
  public String getAdTagDataMode() {
    return getOrElse(
        cachedRemoteConfig.getAndroidAdTagDataMode(),
        DefaultConfig.AD_TAG_DATA_MODE
    );
  }

}
