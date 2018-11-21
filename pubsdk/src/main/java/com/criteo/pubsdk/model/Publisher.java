package com.criteo.pubsdk.model;

import android.content.Context;
import com.criteo.pubsdk.BuildConfig;
import com.criteo.pubsdk.Util.HostAppUtil;
import com.google.gson.JsonObject;

public class Publisher {
    private static final String BUNDLE_ID = "bundleid";
    private static final String PUBLISHER_ID = "publisherid";

    private String bundleId;
    private String publisherId;

    public Publisher(Context context) {
        bundleId = BuildConfig.APPLICATION_ID;
        publisherId = HostAppUtil.getPublisherId(context);
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(BUNDLE_ID, bundleId);
        json.addProperty(PUBLISHER_ID, publisherId);
        return json;
    }
}
