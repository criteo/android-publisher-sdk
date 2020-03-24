package com.criteo.publisher.network;

import com.criteo.publisher.BuildConfig;

public class NetworkConfiguration {

  String getCdbUrl() {
    return BuildConfig.cdbUrl;
  }

  String getRemoteConfigUrl() {
    return BuildConfig.remoteConfigUrl;
  }

  String getEventUrl() {
    return BuildConfig.eventUrl;
  }

}
