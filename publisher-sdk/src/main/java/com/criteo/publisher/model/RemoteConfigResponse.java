package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class RemoteConfigResponse {

  @NonNull
  public static RemoteConfigResponse create(
      @Nullable Boolean killSwitch,
      @Nullable String androidDisplayUrlMacro,
      @Nullable String androidAdTagUrlMode,
      @Nullable String androidAdTagDataMacro,
      @Nullable String androidAdTagDataMode,
      @Nullable Boolean csmEnabled
  ) {
    return new AutoValue_RemoteConfigResponse(
        killSwitch,
        androidDisplayUrlMacro,
        androidAdTagUrlMode,
        androidAdTagDataMacro,
        androidAdTagDataMode,
        csmEnabled
    );
  }

  @NonNull
  public static RemoteConfigResponse createEmpty() {
    return create(
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  @NonNull
  RemoteConfigResponse withKillSwitch(@Nullable Boolean killSwitch) {
    return create(
        killSwitch,
        getAndroidDisplayUrlMacro(),
        getAndroidAdTagUrlMode(),
        getAndroidAdTagDataMacro(),
        getAndroidAdTagDataMode(),
        getCsmEnabled()
    );
  }

  public static TypeAdapter<RemoteConfigResponse> typeAdapter(Gson gson) {
    return new AutoValue_RemoteConfigResponse.GsonTypeAdapter(gson);
  }

  /**
   * The kill switch applies to both the iOS and Android SDKs, and tells the SDK to stop getting
   * bids from CDB. Once the kill switch has been set, you may get up to one bid per slot, since the
   * first call to CDB may be initiated prior to the response from the config endpoint. However,
   * that will happen only the first time the app is started after the kill switch is set, because
   * the switch value is persisted (SharedPreferences for Android, UserDefaults for iOS).
   */
  @Nullable
  public abstract Boolean getKillSwitch();

  /**
   * e.g. %%displayUrl%%, replaced by the {@linkplain Slot#getDisplayUrl() displayUrl} provided by
   * CDB, in the wrapper HTML that is loaded in a {@link android.webkit.WebView}.
   */
  @Nullable
  @SerializedName("AndroidDisplayUrlMacro")
  abstract String getAndroidDisplayUrlMacro();

  /**
   * Wrapper HTML that will contain the displayUrl, e.g. :
   * <pre><code>
   *   &lt;html&gt;
   *     &lt;body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'&gt;
   *       &lt;script src=\"%%displayUrl%%\"&gt;
   *       &lt;/script&gt;
   *     &lt;/body&gt;
   *   &lt;/html&gt;
   * </code></pre>
   * <p>
   * In this example {@linkplain #getAndroidDisplayUrlMacro() %%displayURL%%} is replaced by the
   * display url value.
   */
  @Nullable
  @SerializedName("AndroidAdTagUrlMode")
  abstract String getAndroidAdTagUrlMode();

  /**
   * e.g. %%adTagData%%, replaced by the contents of {@linkplain Slot#getDisplayUrl() displayUrl},
   * meaning the JavaScript code to display the ad.
   */
  @Nullable
  @SerializedName("AndroidAdTagDataMacro")
  abstract String getAndroidAdTagDataMacro();

  /**
   * Wrapper HTML that will contain the JavaScript code, e.g.
   * <pre><code>
   *   &lt;html&gt;
   *     &lt;body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'&gt;
   *       &lt;script&gt;%%adTagData%%&lt;/script&gt;
   *       &lt;/body&gt;
   *   &lt;/html&gt;
   * </code></pre>
   * <p>
   * In this example {@linkplain #getAndroidAdTagDataMacro() %%adTagData%%} is replaced by the
   * JavaScript code provided by display url.
   */
  @Nullable
  @SerializedName("AndroidAdTagDataMode")
  abstract String getAndroidAdTagDataMode();

  /**
   * Feature flag for activating/deactivating the CSM feature. If set to <code>true</code>, then the
   * feature is activated. If <code>false</code>, then it is deactivated. If the flag is not present
   * (i.e. equals to <code>null</code>), then the previous persisted value of this flag is taken. If
   * there is no previous value, this means that this is a fresh start of a new application, then a
   * default value is taken.
   */
  @Nullable
  public abstract Boolean getCsmEnabled();

}
