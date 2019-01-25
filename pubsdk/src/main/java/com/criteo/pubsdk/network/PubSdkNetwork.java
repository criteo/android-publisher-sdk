package com.criteo.pubsdk.network;

import android.content.Context;

import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Config;

public final class PubSdkNetwork {

    private PubSdkNetwork() {
    }

    public static Config loadConfig(Context context, int networkId, String appId, String sdkVersion) {
        return PubSdkApi.loadConfig(context, networkId, appId, sdkVersion);
    }

    public static Cdb loadCdb(Context context, Cdb cdb) {
        return PubSdkApi.loadCdb(context, cdb);
    }
}
