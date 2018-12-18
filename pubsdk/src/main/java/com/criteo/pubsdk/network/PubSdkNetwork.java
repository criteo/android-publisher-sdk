package com.criteo.pubsdk.network;

import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Config;

public final class PubSdkNetwork {

    private PubSdkNetwork() {
    }

    public static Config loadConfig(String publisherId, String appId, String sdkVersion) {
        return PubSdkApi.loadConfig(publisherId, appId, sdkVersion);
    }

    public static Cdb loadCdb(Cdb cdb) {
        return PubSdkApi.loadCdb(cdb);
    }
}
