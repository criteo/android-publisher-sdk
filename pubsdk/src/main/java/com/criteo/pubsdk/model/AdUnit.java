package com.criteo.pubsdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdUnit implements Parcelable {

    private static final String PLACEMENT_ID = "placementId";
    private static final String SIZES = "sizes";
    private String placementId;
    private AdSize adSize;

    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
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
        adUnitSizes.put(adSize.getFormattedSize());
        if (adSize != null) {
            adUnitJson.put(SIZES, adUnitSizes);
        }
        adUnitJson.put(PLACEMENT_ID, placementId);
        return adUnitJson;
    }

    @Override
    public String toString() {
        return "AdUnit{" +
                "placementId='" + placementId + '\'' +
                ", adSize=" + adSize +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placementId);
        dest.writeParcelable(this.adSize, flags);
    }

    public AdUnit() {
    }

    protected AdUnit(Parcel in) {
        this.placementId = in.readString();
        this.adSize = in.readParcelable(AdSize.class.getClassLoader());
    }

    public static final Parcelable.Creator<AdUnit> CREATOR = new Parcelable.Creator<AdUnit>() {
        @Override
        public AdUnit createFromParcel(Parcel source) {
            return new AdUnit(source);
        }

        @Override
        public AdUnit[] newArray(int size) {
            return new AdUnit[size];
        }
    };
}
