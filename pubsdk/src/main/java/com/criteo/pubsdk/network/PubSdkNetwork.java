package com.criteo.pubsdk.network;

import android.content.Context;

import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Config;
import com.google.gson.JsonObject;

public final class PubSdkNetwork {

    private PubSdkNetwork() {
    }

    public static Config loadConfig(Context context, int networkId, String appId, String sdkVersion) {
        return PubSdkApi.loadConfig(context, networkId, appId, sdkVersion);
    }

    public static Cdb loadCdb(Context context, Cdb cdb, String userAgent) {
        return PubSdkApi.loadCdb(context, cdb, userAgent);
    }

    public static JsonObject postEvent(Context context, int senderId,
                                       String appId, String gaid, String eventType, int limitedAdTracking) {
        return PubSdkApi.postAppEvent(context, senderId, appId, gaid, eventType, limitedAdTracking);
    }
}
