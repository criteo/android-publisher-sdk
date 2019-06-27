package com.criteo.publisher.model;

import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CacheAdUnit {

    private static final String PLACEMENT_ID = "placementId";
    private static final String SIZES = "sizes";
    private String adUnitId;
    private AdSize adSize;

    public String getPlacementId() {
        return adUnitId;
    }

    public AdSize getSize() {
        return adSize;
    }

    public void setSize(AdSize size) {
        this.adSize = size;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject adUnitJson = new JSONObject();
        JSONArray adUnitSizes = new JSONArray();
        if (adSize != null) {
            adUnitSizes.put(adSize.getFormattedSize());
            adUnitJson.put(SIZES, adUnitSizes);
        }
        adUnitJson.put(PLACEMENT_ID, adUnitId);
        return adUnitJson;
    }

    @Override
    public String toString() {
        return "CacheAdUnit{" +
                "placementId='" + adUnitId + '\'' +
                ", adSize=" + adSize +
                '}';
    }

    public CacheAdUnit(AdSize adSize, String adUnitId) {
        this.adSize = adSize;
        this.adUnitId = adUnitId;
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
                Objects.equals(adSize, cacheAdUnit.adSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adUnitId, adSize);
    }
}
