package com.criteo.publisher.model;

import android.support.annotation.NonNull;

public class RemoteConfigRequestFactory {

  @NonNull
  private final User user;

  @NonNull
  private final Publisher publisher;

  public RemoteConfigRequestFactory(
      @NonNull User user,
      @NonNull Publisher publisher
  ) {
    this.user = user;
    this.publisher = publisher;
  }

  @NonNull
  public RemoteConfigRequest createRequest() {
    return new RemoteConfigRequest(
        publisher.getCriteoPublisherId(),
        publisher.getBundleId(),
        user.getSdkVersion()
    );
  }
}
