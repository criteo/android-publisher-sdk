package com.criteo.publisher.model;

import android.support.annotation.NonNull;

public class RemoteConfigRequest {

  @NonNull
  private final String criteoPublisherId;

  @NonNull
  private final String bundleId;

  @NonNull
  private final String sdkVersion;

  public RemoteConfigRequest(
      @NonNull String criteoPublisherId,
      @NonNull String bundleId,
      @NonNull String sdkVersion) {
    this.criteoPublisherId = criteoPublisherId;
    this.bundleId = bundleId;
    this.sdkVersion = sdkVersion;
  }

  @NonNull
  public String getCriteoPublisherId() {
    return criteoPublisherId;
  }

  @NonNull
  public String getBundleId() {
    return bundleId;
  }

  @NonNull
  public String getSdkVersion() {
    return sdkVersion;
  }

}
