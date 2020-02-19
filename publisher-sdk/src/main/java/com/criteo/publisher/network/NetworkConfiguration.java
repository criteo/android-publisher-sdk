package com.criteo.publisher.network;

import com.criteo.publisher.BuildConfig;

public class NetworkConfiguration {

  String getCdbUrl() {
    return BuildConfig.CDB_URL;
  }

  String getRemoteConfigUrl() {
    return BuildConfig.REMOTE_CONFIG_URL;
  }

  String getEventUrl() {
    return BuildConfig.EVENT_URL;
  }

}
