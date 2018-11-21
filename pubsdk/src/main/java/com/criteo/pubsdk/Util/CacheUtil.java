package com.criteo.pubsdk.Util;

import com.google.gson.JsonObject;

/**
 * TODO: this will be used for in memory cache
 */
public class CacheUtil {

    private long timeOfDownload;
    private JsonObject object;

    public long getTimeOfDownload() {
        return timeOfDownload;
    }

    public void setTimeOfDownload(long timeOfDownload) {
        this.timeOfDownload = timeOfDownload;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    /**
     *
     * @return true/false based on user
     */
    public boolean isValid(){
        return true;
    }
}
