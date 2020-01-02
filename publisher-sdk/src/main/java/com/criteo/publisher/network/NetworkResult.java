package com.criteo.publisher.network;

import android.support.annotation.Nullable;
import com.criteo.publisher.model.CdbResponse;

import org.json.JSONObject;

public class NetworkResult {

    @Nullable
    private CdbResponse cdbResponse;

    @Nullable
    private JSONObject config;

    NetworkResult(@Nullable CdbResponse cdbResponse, @Nullable JSONObject config) {
        this.cdbResponse = cdbResponse;
        this.config = config;
    }

    @Nullable
    public CdbResponse getCdbResponse() {
        return cdbResponse;
    }

    @Nullable
    public JSONObject getConfig() {
        return config;
    }

}
