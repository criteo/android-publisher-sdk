package com.criteo.pubsdk.model;

import com.google.gson.JsonObject;

public class Slot {
    private static final String IMP_ID = "impid";
    private static final String CPM = "cpm";
    private static final String DISPLAY_URL = "displayurl";
    private static final String ZONE_ID = "zoneid";
    private static final String STATUS = "status";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";

    private String impId;
    private float cpm;
    private String displayUrl;
    private int zoneId;
    private int status;
    private int width;
    private int height;

    public String getImpId() {
        return impId;
    }

    public void setImpId(String impId) {
        this.impId = impId;
    }

    public float getCpm() {
        return cpm;
    }

    public void setCpm(float cpm) {
        this.cpm = cpm;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

/*    public JSONObject toJson() throws JSONException {
        JSONObject json=new JSONObject();
        json.put(IMP_ID, impId);
        json.put(CPM, cpm);
        json.put(DISPLAY_URL, displayUrl);
        json.put(ZONE_ID, zoneId);
        json.put(STATUS, status);
        json.put(WIDTH, width);
        json.put(HEIGHT, height);
        return json;
    }*/

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(IMP_ID, impId);
        json.addProperty(CPM, cpm);
        json.addProperty(DISPLAY_URL, displayUrl);
        json.addProperty(ZONE_ID, zoneId);
        json.addProperty(STATUS, status);
        json.addProperty(WIDTH, width);
        json.addProperty(HEIGHT, height);
        return json;
    }


}
