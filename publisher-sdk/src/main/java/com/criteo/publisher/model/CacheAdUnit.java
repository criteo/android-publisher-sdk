package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CacheAdUnit {

    private static final String PLACEMENT_ID = "placementId";
    private static final String SIZES = "sizes";
    private static final String IS_NATIVE = "isNative";
    private static final String IS_INTERSTITIAL = "interstitial";
    private String adUnitId;
    private AdSize adSize;
    private AdUnitType adUnitType;

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

        switch (adUnitType) {
            case CRITEO_INTERSTITIAL:
                adUnitJson.put(IS_INTERSTITIAL, true);
                break;
            case CRITEO_NATIVE:
                adUnitJson.put(IS_NATIVE, true);
                break;
        }

        return adUnitJson;
    }

    @Override
    public String toString() {
        return "CacheAdUnit{" +
                "placementId='" + adUnitId + '\'' +
                ", adSize=" + adSize +
                ", adUnitType= " + adUnitType +
                '}';
    }

    public CacheAdUnit(AdSize adSize, String adUnitId, AdUnitType adUnitType) {
        this.adSize = adSize;
        this.adUnitId = adUnitId;
        this.adUnitType = adUnitType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheAdUnit that = (CacheAdUnit) o;

        if (adUnitId != null ? !adUnitId.equals(that.adUnitId) : that.adUnitId != null) {
            return false;
        }
        if (adSize != null ? !adSize.equals(that.adSize) : that.adSize != null) {
            return false;
        }
        return adUnitType == that.adUnitType;
    }

    @Override
    public int hashCode() {
        int result = adUnitId != null ? adUnitId.hashCode() : 0;
        result = 31 * result + (adSize != null ? adSize.hashCode() : 0);
        result = 31 * result + (adUnitType != null ? adUnitType.hashCode() : 0);
        return result;
    }
}
