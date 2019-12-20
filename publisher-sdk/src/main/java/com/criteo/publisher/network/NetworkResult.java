package com.criteo.publisher.network;

import android.support.annotation.Nullable;
import com.criteo.publisher.model.Cdb;

import org.json.JSONObject;

public class NetworkResult {

    @Nullable
    private Cdb cdb;

    @Nullable
    private JSONObject config;

    NetworkResult(@Nullable Cdb cdb, @Nullable JSONObject config) {
        this.cdb = cdb;
        this.config = config;
    }

    @Nullable
    public Cdb getCdb() {
        return cdb;
    }

    @Nullable
    public JSONObject getConfig() {
        return config;
    }

}
