package com.criteo.publisher.model;

import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CacheAdUnit {

    private static final String PLACEMENT_ID = "placementId";
    private static final String SIZES = "sizes";
    private static final String IS_NATIVE = "isNative";
    private String adUnitId;
    private AdSize adSize;
    private boolean isNative;

    public String getPlacementId() {
        return adUnitId;
    }

    public AdSize getSize() {
        return adSize;
    }

    public void setSize(AdSize size) {
        this.adSize = size;
    }

    public boolean isNative() {
        return this.isNative;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject adUnitJson = new JSONObject();
        JSONArray adUnitSizes = new JSONArray();
        if (adSize != null) {
            adUnitSizes.put(adSize.getFormattedSize());
            adUnitJson.put(SIZES, adUnitSizes);
        }
        adUnitJson.put(PLACEMENT_ID, adUnitId);
        if(this.isNative) {
            adUnitJson.put(IS_NATIVE, this.isNative);
        }
        return adUnitJson;
    }

    @Override
    public String toString() {
        return "CacheAdUnit{" +
                "placementId='" + adUnitId + '\'' +
                ", adSize=" + adSize +
                ", isNative= " + isNative +
                '}';
    }

    public CacheAdUnit(AdSize adSize, String adUnitId, boolean isNative) {
        this.adSize = adSize;
        this.adUnitId = adUnitId;
        this.isNative = isNative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CacheAdUnit cacheAdUnit = (CacheAdUnit) o;
        return Objects.equals(adUnitId, cacheAdUnit.adUnitId) &&
                Objects.equals(adSize, cacheAdUnit.adSize) &&
                Objects.equals(isNative, cacheAdUnit.isNative);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adUnitId, adSize, isNative);
    }

}
