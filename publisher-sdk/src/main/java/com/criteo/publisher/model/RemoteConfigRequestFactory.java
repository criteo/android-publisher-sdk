package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import com.criteo.publisher.util.BuildConfigWrapper;

public class RemoteConfigRequestFactory {

  @NonNull
  private final Publisher publisher;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public RemoteConfigRequestFactory(
      @NonNull Publisher publisher,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.publisher = publisher;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  public RemoteConfigRequest createRequest() {
    return new RemoteConfigRequest(
        publisher.getCriteoPublisherId(),
        publisher.getBundleId(),
        buildConfigWrapper.getSdkVersion()
    );
  }
}
