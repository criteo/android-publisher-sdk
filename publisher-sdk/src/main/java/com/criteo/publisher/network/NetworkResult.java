package com.criteo.publisher.network;

import com.criteo.publisher.model.Cdb;

import org.json.JSONObject;

public class NetworkResult {

    private Cdb cdb;
    private JSONObject config;

    NetworkResult(Cdb cdb, JSONObject config) {
        this.cdb = cdb;
        this.config = config;
    }

    NetworkResult() {
    }

    public Cdb getCdb() {
        return cdb;
    }

    public void setCdb(Cdb cdb) {
        this.cdb = cdb;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }
}
