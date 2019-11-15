package com.criteo.publisher.network;

import com.criteo.publisher.network.PubSdkApi.PubSdkApiHolder;

public class PubSdkApiHelper {

  public static void withApi(PubSdkApi api, Runnable runnable) {
    PubSdkApi oldInstance = PubSdkApiHolder.instance;
    try {
      PubSdkApiHolder.instance = api;
      runnable.run();
    } finally {
      PubSdkApiHolder.instance = oldInstance;
    }
  }

}
