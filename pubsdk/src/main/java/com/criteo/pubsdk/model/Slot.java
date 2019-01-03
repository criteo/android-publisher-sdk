package com.criteo.pubsdk.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Slot {

    private static final String SLOT_ID = "slotId";
    private static final String IMP_ID = "impId";
    private static final String ZONE_ID = "zoneId";
    private static final String CPM = "cpm";
    private static final String CURRENCY = "currency";
    private static final String CREATIVE = "creative";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String PLACEMENT_ID = "placementId";
    private static final String SIZES = "sizes";
    private static final String NATIVE = "isNative";
    private static final String TTL = "ttl";
    private static final int DEFAULT_TTL = 0;

    private String slotId;
    private String impId;
    private float cpm;
    private String currency;
    private String creative;
    private int zoneId;
    private int width;
    private int height;
    private String placementId;
    private List<String> sizes;
    private boolean nativeImpression;
    private int ttl;
    //require for cache
    private long timeOfDownload;

    public Slot() {
        sizes = new ArrayList<>();
    }

    public Slot(JsonObject json) {
        placementId = json.has(PLACEMENT_ID) ? json.get(PLACEMENT_ID).getAsString() : null;
        impId = json.has(IMP_ID) ? json.get(IMP_ID).getAsString() : null;
        slotId = json.has(SLOT_ID) ? json.get(SLOT_ID).getAsString() : null;
        zoneId = json.has(ZONE_ID) ? json.get(ZONE_ID).getAsInt() : 0;
        cpm = json.has(CPM) ? json.get(CPM).getAsFloat() : 0.0f;
        currency = json.has(CURRENCY) ? json.get(CURRENCY).getAsString() : null;
        width = json.has(WIDTH) ? json.get(WIDTH).getAsInt() : 0;
        height = json.has(HEIGHT) ? json.get(HEIGHT).getAsInt() : 0;
        creative = json.has(CREATIVE) ? json.get(CREATIVE).getAsString() : null;
        sizes = new ArrayList<>();
        ttl = json.has(TTL) ? json.get(TTL).getAsInt() : DEFAULT_TTL;
        timeOfDownload = System.currentTimeMillis();
    }

    public String getPlacementId() {
        return placementId;
    }

    public boolean isNativeImpression() {
        return nativeImpression;
    }

    public void setNativeImpression(boolean nativeImpression) {
        this.nativeImpression = nativeImpression;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

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

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
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

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public void addSize(String size) {
        this.sizes.add(size);
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public long getTimeOfDownload() {
        return timeOfDownload;
    }

    public void setTimeOfDownload(long timeOfDownload) {
        this.timeOfDownload = timeOfDownload;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (sizes.size() > 0) {
            TypeToken<ArrayList<String>> token = new TypeToken<ArrayList<String>>() {
            };
            Gson gson = new Gson();
            json.addProperty(SIZES, gson.toJson(sizes, token.getType()));
        }
        if (zoneId > 0) {
            json.addProperty(ZONE_ID, zoneId);
        }
        json.addProperty(PLACEMENT_ID, placementId);
        if (nativeImpression) {
            json.addProperty(NATIVE, nativeImpression);
        }
        return json;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "slotId='" + slotId + '\'' +
                ", impId='" + impId + '\'' +
                ", cpm=" + cpm +
                ", currency='" + currency + '\'' +
                ", creative='" + creative + '\'' +
                ", zoneId=" + zoneId +
                ", width=" + width +
                ", height=" + height +
                ", placementId='" + placementId + '\'' +
                ", sizes=" + sizes +
                ", nativeImpression=" + nativeImpression +
                ", ttl=" + ttl +
                ", timeOfDownload=" + timeOfDownload +
                '}';
    }
}