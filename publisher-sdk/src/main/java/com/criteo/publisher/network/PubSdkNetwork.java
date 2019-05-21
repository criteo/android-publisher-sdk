package com.criteo.publisher.network;

import android.content.Context;

import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.Config;

import org.json.JSONObject;

public final class PubSdkNetwork {

    private PubSdkNetwork() {
    }

    public static Config loadConfig(Context context, String criteoPublisherId, String appId, String sdkVersion) {
        return PubSdkApi.loadConfig(context, criteoPublisherId, appId, sdkVersion);
    }

    public static Cdb loadCdb(Context context, Cdb cdb, String userAgent) {
        return PubSdkApi.loadCdb(context, cdb, userAgent);
    }

    public static JSONObject postEvent(Context context, int senderId,
                                       String appId, String gaid, String eventType, int limitedAdTracking) {
        return PubSdkApi.postAppEvent(context, senderId, appId, gaid, eventType, limitedAdTracking);
    }
}
