package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import com.criteo.publisher.BuildConfig;

/**
 * Simple wrapper around {@link BuildConfig} constants to ease testing
 */
public class BuildConfigWrapper {

  @NonNull
  public String getSdkVersion() {
    return BuildConfig.VERSION_NAME;
  }

  @NonNull
  public String getCdbUrl() {
    return BuildConfig.cdbUrl;
  }

  @NonNull
  public String getRemoteConfigUrl() {
    return BuildConfig.remoteConfigUrl;
  }

  @NonNull
  public String getEventUrl() {
    return BuildConfig.eventUrl;
  }
}
